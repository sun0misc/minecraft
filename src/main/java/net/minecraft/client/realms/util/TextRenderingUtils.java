/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.client.realms.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TextRenderingUtils {
    private TextRenderingUtils() {
    }

    @VisibleForTesting
    protected static List<String> lineBreak(String text) {
        return Arrays.asList(text.split("\\n"));
    }

    public static List<Line> decompose(String text, LineSegment ... links) {
        return TextRenderingUtils.decompose(text, Arrays.asList(links));
    }

    private static List<Line> decompose(String text, List<LineSegment> links) {
        List<String> list2 = TextRenderingUtils.lineBreak(text);
        return TextRenderingUtils.insertLinks(list2, links);
    }

    private static List<Line> insertLinks(List<String> lines, List<LineSegment> links) {
        int i = 0;
        ArrayList<Line> list3 = Lists.newArrayList();
        for (String string : lines) {
            ArrayList<LineSegment> list4 = Lists.newArrayList();
            List<String> list5 = TextRenderingUtils.split(string, "%link");
            for (String string2 : list5) {
                if ("%link".equals(string2)) {
                    list4.add(links.get(i++));
                    continue;
                }
                list4.add(LineSegment.text(string2));
            }
            list3.add(new Line(list4));
        }
        return list3;
    }

    public static List<String> split(String line, String delimiter) {
        int j;
        if (delimiter.isEmpty()) {
            throw new IllegalArgumentException("Delimiter cannot be the empty string");
        }
        ArrayList<String> list = Lists.newArrayList();
        int i = 0;
        while ((j = line.indexOf(delimiter, i)) != -1) {
            if (j > i) {
                list.add(line.substring(i, j));
            }
            list.add(delimiter);
            i = j + delimiter.length();
        }
        if (i < line.length()) {
            list.add(line.substring(i));
        }
        return list;
    }

    @Environment(value=EnvType.CLIENT)
    public static class LineSegment {
        private final String fullText;
        @Nullable
        private final String linkTitle;
        @Nullable
        private final String linkUrl;

        private LineSegment(String fullText) {
            this.fullText = fullText;
            this.linkTitle = null;
            this.linkUrl = null;
        }

        private LineSegment(String fullText, @Nullable String linkTitle, @Nullable String linkUrl) {
            this.fullText = fullText;
            this.linkTitle = linkTitle;
            this.linkUrl = linkUrl;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            LineSegment lv = (LineSegment)o;
            return Objects.equals(this.fullText, lv.fullText) && Objects.equals(this.linkTitle, lv.linkTitle) && Objects.equals(this.linkUrl, lv.linkUrl);
        }

        public int hashCode() {
            return Objects.hash(this.fullText, this.linkTitle, this.linkUrl);
        }

        public String toString() {
            return "Segment{fullText='" + this.fullText + "', linkTitle='" + this.linkTitle + "', linkUrl='" + this.linkUrl + "'}";
        }

        public String renderedText() {
            return this.isLink() ? this.linkTitle : this.fullText;
        }

        public boolean isLink() {
            return this.linkTitle != null;
        }

        public String getLinkUrl() {
            if (!this.isLink()) {
                throw new IllegalStateException("Not a link: " + String.valueOf(this));
            }
            return this.linkUrl;
        }

        public static LineSegment link(String linkTitle, String linkUrl) {
            return new LineSegment(null, linkTitle, linkUrl);
        }

        @VisibleForTesting
        protected static LineSegment text(String fullText) {
            return new LineSegment(fullText);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Line {
        public final List<LineSegment> segments;

        Line(LineSegment ... segments) {
            this(Arrays.asList(segments));
        }

        Line(List<LineSegment> segments) {
            this.segments = segments;
        }

        public String toString() {
            return "Line{segments=" + String.valueOf(this.segments) + "}";
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            Line lv = (Line)o;
            return Objects.equals(this.segments, lv.segments);
        }

        public int hashCode() {
            return Objects.hash(this.segments);
        }
    }
}

