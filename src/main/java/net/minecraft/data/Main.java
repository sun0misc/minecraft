package net.minecraft.data;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import net.minecraft.GameVersion;
import net.minecraft.SharedConstants;
import net.minecraft.data.client.ModelProvider;
import net.minecraft.data.dev.NbtProvider;
import net.minecraft.data.report.BlockListProvider;
import net.minecraft.data.report.CommandSyntaxProvider;
import net.minecraft.data.report.DynamicRegistriesProvider;
import net.minecraft.data.report.RegistryDumpProvider;
import net.minecraft.data.server.BiomeParametersProvider;
import net.minecraft.data.server.advancement.vanilla.VanillaAdvancementProviders;
import net.minecraft.data.server.loottable.vanilla.VanillaLootTableProviders;
import net.minecraft.data.server.recipe.BundleRecipeProvider;
import net.minecraft.data.server.recipe.VanillaRecipeProvider;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaBannerPatternTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaBiomeTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaBlockTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaCatVariantTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaDamageTypeTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaEntityTypeTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaFlatLevelGeneratorPresetTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaFluidTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaGameEventTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaInstrumentTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaItemTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaPaintingVariantTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaPointOfInterestTypeTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaStructureTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaWorldPresetTagProvider;
import net.minecraft.data.validate.StructureValidatorProvider;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public class Main {
   @DontObfuscate
   public static void main(String[] args) throws IOException {
      SharedConstants.createGameVersion();
      OptionParser optionParser = new OptionParser();
      OptionSpec optionSpec = optionParser.accepts("help", "Show the help menu").forHelp();
      OptionSpec optionSpec2 = optionParser.accepts("server", "Include server generators");
      OptionSpec optionSpec3 = optionParser.accepts("client", "Include client generators");
      OptionSpec optionSpec4 = optionParser.accepts("dev", "Include development tools");
      OptionSpec optionSpec5 = optionParser.accepts("reports", "Include data reports");
      OptionSpec optionSpec6 = optionParser.accepts("validate", "Validate inputs");
      OptionSpec optionSpec7 = optionParser.accepts("all", "Include all generators");
      OptionSpec optionSpec8 = optionParser.accepts("output", "Output folder").withRequiredArg().defaultsTo("generated", new String[0]);
      OptionSpec optionSpec9 = optionParser.accepts("input", "Input folder").withRequiredArg();
      OptionSet optionSet = optionParser.parse(args);
      if (!optionSet.has(optionSpec) && optionSet.hasOptions()) {
         Path path = Paths.get((String)optionSpec8.value(optionSet));
         boolean bl = optionSet.has(optionSpec7);
         boolean bl2 = bl || optionSet.has(optionSpec3);
         boolean bl3 = bl || optionSet.has(optionSpec2);
         boolean bl4 = bl || optionSet.has(optionSpec4);
         boolean bl5 = bl || optionSet.has(optionSpec5);
         boolean bl6 = bl || optionSet.has(optionSpec6);
         DataGenerator lv = create(path, (Collection)optionSet.valuesOf(optionSpec9).stream().map((input) -> {
            return Paths.get(input);
         }).collect(Collectors.toList()), bl2, bl3, bl4, bl5, bl6, SharedConstants.getGameVersion(), true);
         lv.run();
      } else {
         optionParser.printHelpOn(System.out);
      }
   }

   private static DataProvider.Factory toFactory(BiFunction baseFactory, CompletableFuture registryLookupFuture) {
      return (output) -> {
         return (DataProvider)baseFactory.apply(output, registryLookupFuture);
      };
   }

   public static DataGenerator create(Path output, Collection inputs, boolean includeClient, boolean includeServer, boolean includeDev, boolean includeReports, boolean validate, GameVersion gameVersion, boolean ignoreCache) {
      DataGenerator lv = new DataGenerator(output, gameVersion, ignoreCache);
      DataGenerator.Pack lv2 = lv.createVanillaPack(includeClient || includeServer);
      lv2.addProvider((outputx) -> {
         return (new SnbtProvider(outputx, inputs)).addWriter(new StructureValidatorProvider());
      });
      CompletableFuture completableFuture = CompletableFuture.supplyAsync(BuiltinRegistries::createWrapperLookup, Util.getMainWorkerExecutor());
      DataGenerator.Pack lv3 = lv.createVanillaPack(includeClient);
      lv3.addProvider(ModelProvider::new);
      DataGenerator.Pack lv4 = lv.createVanillaPack(includeServer);
      lv4.addProvider(toFactory(DynamicRegistriesProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaAdvancementProviders::createVanillaProvider, completableFuture));
      lv4.addProvider(VanillaLootTableProviders::createVanillaProvider);
      lv4.addProvider(VanillaRecipeProvider::new);
      TagProvider lv5 = (TagProvider)lv4.addProvider(toFactory(VanillaBlockTagProvider::new, completableFuture));
      TagProvider lv6 = (TagProvider)lv4.addProvider((outputx) -> {
         return new VanillaItemTagProvider(outputx, completableFuture, lv5.getTagLookupFuture());
      });
      lv4.addProvider(toFactory(VanillaBannerPatternTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaBiomeTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaCatVariantTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaDamageTypeTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaEntityTypeTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaFlatLevelGeneratorPresetTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaFluidTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaGameEventTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaInstrumentTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaPaintingVariantTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaPointOfInterestTypeTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaStructureTagProvider::new, completableFuture));
      lv4.addProvider(toFactory(VanillaWorldPresetTagProvider::new, completableFuture));
      lv4 = lv.createVanillaPack(includeDev);
      lv4.addProvider((outputx) -> {
         return new NbtProvider(outputx, inputs);
      });
      lv4 = lv.createVanillaPack(includeReports);
      lv4.addProvider(toFactory(BiomeParametersProvider::new, completableFuture));
      lv4.addProvider(BlockListProvider::new);
      lv4.addProvider(toFactory(CommandSyntaxProvider::new, completableFuture));
      lv4.addProvider(RegistryDumpProvider::new);
      lv4 = lv.createVanillaSubPack(includeServer, "bundle");
      lv4.addProvider(BundleRecipeProvider::new);
      lv4.addProvider((outputx) -> {
         return MetadataProvider.create(outputx, Text.translatable("dataPack.bundle.description"), FeatureSet.of(FeatureFlags.BUNDLE));
      });
      return lv;
   }
}
