package rip.ada.groups.scorecards;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ScorecardPrinter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScorecardPrinter.class);
    private static final PDType1Font HELVETICA = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font HELVETICA_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final ScorecardPaginator scorecardPaginator = new ScorecardPaginator();
    private final List<PDType0Font> fallbackFonts = new ArrayList<>();

    public PDDocument printScorecards(final ScorecardSet scorecardSet) {
        final PDDocument doc = new PDDocument();
        if (fallbackFonts.isEmpty()) {
            try {
                try (Stream<Path> list = Files.list(Path.of("/opt/cubing/groups/fonts"))) {
                    list
                            .filter(path -> path.toString().toLowerCase().endsWith(".ttf"))
                            .forEach(path -> {
                                try {
                                    fallbackFonts.add(PDType0Font.load(doc, Files.newInputStream(path)));
                                } catch (IOException e) {
                                    LOGGER.warn("Failed to load font: {} - {}", path, e.getMessage());
                                }
                            });
                }
            } catch (final IOException e) {
                LOGGER.warn("Failed to access fonts directory: {}", e.getMessage());
            }
        }
        for (final ScorecardPage page : scorecardPaginator.getScorecardPages(scorecardSet)) {
            addPage(doc, page);
        }

        return doc;
    }

    private void addPage(final PDDocument pdf, final ScorecardPage scorecardPage) {
        final PDRectangle pageSize = PDRectangle.A4;
        final PDPage page = new PDPage(pageSize);
        pdf.addPage(page);
        try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
            final float midX = pageSize.getLowerLeftX() + pageSize.getWidth() / 2f;
            final float midY = pageSize.getLowerLeftY() + pageSize.getHeight() / 2f;

            dividePageIntoQuadrants(content, pageSize, midX, midY);

            final float[][] quadrants = new float[][]{
                    new float[]{pageSize.getLowerLeftX(), midX, pageSize.getUpperRightY(), midY},
                    new float[]{midX, pageSize.getUpperRightX(), pageSize.getUpperRightY(), midY},
                    new float[]{pageSize.getLowerLeftX(), midX, midY, pageSize.getLowerLeftY()},
                    new float[]{midX, pageSize.getUpperRightX(), midY, pageSize.getLowerLeftY()},
            };

            for (int i = 0; i < 4; i++) {
                if (scorecardPage.scorecards().size() <= i) {
                    break;
                }

                final Scorecard scorecard = scorecardPage.scorecards().get(i);
                final float[] coordinates = quadrants[i];

                drawScorecardInRectangle(content, coordinates[0], coordinates[1], coordinates[2], coordinates[3], scorecard);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawScorecardInRectangle(final PDPageContentStream content, final float minX, final float maxX, final float minY, final float maxY, final Scorecard scorecard) {
        if (scorecard.fixedSeating()) {
            drawCenteredText(content, HELVETICA_BOLD, 12, minX + (maxX - minX) / 2, minY - 10, "*** Fixed Seating ***");
        }
        drawText(content, HELVETICA, 8, minX + 10, minY - 10 - 10, String.valueOf(scorecard.seed()));
        drawCenteredText(content, HELVETICA_BOLD, 16, minX + (maxX - minX) / 2, minY - 10 - 20, scorecard.competitionName());

        drawText(content, HELVETICA, 8, minX + 33, minY - 10 - 42, "Event");
        if (scorecard.stationNumber() != null) {
            drawText(content, HELVETICA, 8, maxX - 120, minY - 10 - 42, "Station");
        }
        drawText(content, HELVETICA, 8, maxX - 80, minY - 10 - 42, "Round");
        drawText(content, HELVETICA, 8, maxX - 40, minY - 10 - 42, "Group");
        drawDividedRect(content, minX + 30, maxX - 10, minY - 10 - 50, minY - 10 - 70, scorecard.stationNumber() != null ? new float[]{0.55f, 0.7f, 0.85f} : new float[]{0.7f, 0.85f});
        drawText(content, HELVETICA, 12, minX + 40, minY - 10 - 55, scorecard.event().getFriendlyName());
        if (scorecard.stationNumber() != null) {

            drawText(content, HELVETICA, 12, maxX - 115, minY - 10 - 55, String.valueOf(scorecard.stationNumber()));
        }
        drawText(content, HELVETICA, 12, maxX - 75, minY - 10 - 55, String.valueOf(scorecard.round()));
        if (scorecard.group() != -1) {
            drawText(content, HELVETICA, 12, maxX - 35, minY - 10 - 55, String.valueOf(scorecard.group()));
        }

        drawDividedRect(content, minX + 30, maxX - 10, minY - 10 - 90, minY - 10 - 110, new float[]{0.15f});
        drawText(content, HELVETICA, 8, minX + 33, minY - 10 - 80, "ID");
        drawText(content, HELVETICA, 8, minX + 35 + (maxX - minX - 10 - 30) * 0.15f, minY - 10 - 80, "Name");
        drawRightAlignedText(content, HELVETICA, 8, maxX - 15, minY - 10 - 80, scorecard.wcaId());
        drawText(content, HELVETICA, 12, minX + 40, minY - 10 - 95, String.valueOf(scorecard.personId()));
        drawText(content, HELVETICA, 12, minX + 35 + (maxX - minX - 10 - 30) * 0.15f, minY - 10 - 95, scorecard.personName());

        drawText(content, HELVETICA, 8, minX + 33, minY - 10 - 130, scorecard.doubleChecked() ? "Scr/Chk" : "Scr");
        drawText(content, HELVETICA, 8, maxX - 70, minY - 10 - 130, "Judge");
        drawText(content, HELVETICA, 8, maxX - 40, minY - 10 - 130, "Comp");
        float y = minY - 10 - 140;
        final float spacing = 7;
        final float height = 30;
        for (int i = 0; i < scorecard.roundFormat().getSolveCount(); i++) {
            if (scorecard.cutoff() != null) {
                if (scorecard.cutoff().numberOfAttempts() == i) {
                    drawDashedLine(content, minX + 10, maxX - 10, y + spacing / 2, y + spacing / 2);
                }
            }
            drawText(content, HELVETICA_BOLD, 15, minX + 17, y - 7, String.valueOf(i + 1));
            drawDividedRect(content, minX + 30, maxX - 10, y, y - height, new float[]{0.12f, 0.76f, 0.88f});
            if (scorecard.doubleChecked()) {
                drawLine(content, minX + 30, minX + 30 + (maxX - minX - 30 - 10) * 0.12f, y - height, y);
            }
            y -= spacing + height;
        }

        drawText(content, HELVETICA, 10, minX + 10, y, "Extra - Reason:");
        y -= 20;

        drawText(content, HELVETICA_BOLD, 15, minX + 17, y - 7, "_");
        drawDividedRect(content, minX + 30, maxX - 10, y, y - height, new float[]{0.12f, 0.76f, 0.88f});
        if (scorecard.doubleChecked()) {
            drawLine(content, minX + 30, minX + 30 + (maxX - minX - 30 - 10) * 0.12f, y - height, y);
        }
        y -= spacing + height;

        if (scorecard.cutoff() != null) {
            drawText(content, HELVETICA, 10, minX + 10, y, "Cutoff: <" + scorecard.cutoff().attemptResult());
        }

        if (scorecard.timeLimit() != null) {
            drawRightAlignedText(content, HELVETICA, 10, maxX - 10, y, (scorecard.timeLimit().cumulativeRoundIds().isEmpty() ? "Time Limit: " : "Cumulative Time Limit: ") + scorecard.timeLimit().centiseconds());
        }
    }

    private void drawText(final PDPageContentStream content, final PDType1Font mainFont, final float fontSize, final float topLeftX, final float topLeftY, final String text) {
        try {
            final float ascent = mainFont.getFontDescriptor().getAscent() / 1000 * fontSize;

            content.beginText();
            content.newLineAtOffset(topLeftX, topLeftY - ascent);

            PDFont currentFont = null;

            for (int i = 0; i < text.length();) {
                final char c = text.charAt(i);
                PDFont font = null;

                if (mainFont.hasGlyph(c)) {
                    font = mainFont;
                } else {
                    boolean found = false;
                    for (final PDType0Font fallbackFont : fallbackFonts) {
                        if (fallbackFont.hasGlyph(c)) {
                            font = fallbackFont;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        i++; // skip unknown character
                        continue;
                    }
                }

                final StringBuilder run = new StringBuilder();
                run.append(c);
                i++;

                while (i < text.length()) {
                    final char next = text.charAt(i);
                    PDFont nextFont = null;
                    if (mainFont.hasGlyph(next)) {
                        nextFont = mainFont;
                    } else if (((PDVectorFont) font).hasGlyph(next)) {
                        nextFont = font;
                    } else {
                        for (final PDType0Font fallbackFont : fallbackFonts) {
                            if (fallbackFont.hasGlyph(next)) {
                                nextFont = fallbackFont;
                                break;
                            }
                        }
                    }
                    if (nextFont == null || nextFont != font) {
                        break;
                    }
                    run.append(next);
                    i++;
                }

                if (font != currentFont) {
                    currentFont = font;
                    content.setFont(font, fontSize);
                }

                try {
                    content.showText(run.toString());
                } catch (final Exception e) {
                    LOGGER.error("Failed to render text", e);
                }
            }

            content.endText();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawCenteredText(final PDPageContentStream content, final PDFont pdFont, final float fontSize, final float middleX, final float topLeftY, final String text) {
        try {
            content.setFont(pdFont, fontSize);
            final float ascent = pdFont.getFontDescriptor().getAscent() / 1000 * fontSize;
            final float stringWidth = pdFont.getStringWidth(text) / 1000 * fontSize;
            final float startX = middleX - stringWidth / 2;

            content.beginText();
            content.newLineAtOffset(startX, topLeftY - ascent);
            content.showText(text);
            content.endText();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawRightAlignedText(final PDPageContentStream content, final PDFont pdFont, final float fontSize, final float endX, final float topLeftY, final String text) {
        try {
            content.setFont(pdFont, fontSize);
            final float ascent = pdFont.getFontDescriptor().getAscent() / 1000 * fontSize;
            final float stringWidth = pdFont.getStringWidth(text) / 1000 * fontSize;
            final float startX = endX - stringWidth;

            content.beginText();
            content.newLineAtOffset(startX, topLeftY - ascent);
            content.showText(text);
            content.endText();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawDividedRect(final PDPageContentStream content, final float minX, final float maxX, final float minY, final float maxY, final float[] divisions) {
        try {
            content.setStrokingColor(0.0f, 0.0f, 0.0f);
            content.setLineWidth(1.0f);
            content.setLineDashPattern(new float[0], 0);
            content.moveTo(minX, minY);
            content.lineTo(maxX, minY);
            content.lineTo(maxX, maxY);
            content.lineTo(minX, maxY);
            content.lineTo(minX, minY);
            for (final float division : divisions) {
                final float offset = minX + (maxX - minX) * division;
                content.moveTo(offset, minY);
                content.lineTo(offset, maxY);
            }
            content.stroke();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawDashedLine(final PDPageContentStream content, final float startX, final float endX, final float startY, final float endY) {
        try {
            content.setLineDashPattern(new float[]{3, 3}, 0);
            content.moveTo(startX, startY);
            content.lineTo(endX, endY);
            content.stroke();
            content.setLineDashPattern(new float[0], 0);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void drawLine(final PDPageContentStream content, final float startX, final float endX, final float startY, final float endY) {
        try {
            content.moveTo(startX, startY);
            content.lineTo(endX, endY);
            content.stroke();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void dividePageIntoQuadrants(final PDPageContentStream content, final PDRectangle pageSize, final float midX, final float midY) throws IOException {
        content.setStrokingColor(0.8f, 0.8f, 0.8f);
        content.setLineWidth(0.5f);
        content.setLineDashPattern(new float[]{3, 3}, 0);
        content.moveTo(midX, pageSize.getLowerLeftY());
        content.lineTo(midX, pageSize.getUpperRightY());
        content.moveTo(pageSize.getLowerLeftX(), midY);
        content.lineTo(pageSize.getUpperRightX(), midY);
        content.stroke();
    }

}
