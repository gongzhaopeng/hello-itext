package com.atzu68.trial.helloitext;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfDiv;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.util.XMLResourceDescriptor;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Example => https://github.com/pbamotra/iText-SVG-PDF
 */
public class ITextWithSvgTest {

    @Test
    public void insertMultipleSVGs() throws Exception {

        final var SVG_FILE_PATHS = List.of("foobarcity_I.svg", "foobarcity_II.svg");

        final var OUTPUT_FILE_PATH = "pdfWithMultipleSVGs.pdf";

        var outputDocument = new Document();
        var outputWriter = PdfWriter.getInstance(
                outputDocument,
                new FileOutputStream(OUTPUT_FILE_PATH)
        );
        outputDocument.open();

        for (String svgFilePath : SVG_FILE_PATHS) {

            final var WIDTH = 6000;
            final var HEIGHT = 6000;
            final var MAX_SVG_PDF_SIZE = 1024 * 1024 * 2;   // 2M

            var svgDocument = new Document(new Rectangle(WIDTH, HEIGHT));
            var svgBaos = new ByteArrayOutputStream(MAX_SVG_PDF_SIZE);
            var svgWriter = PdfWriter.getInstance(svgDocument, svgBaos);

            svgDocument.open();

            var cb = svgWriter.getDirectContent();
            var map = cb.createTemplate(WIDTH, HEIGHT);
            drawSvg(map, svgFilePath, WIDTH, HEIGHT);
            cb.addTemplate(map, 0, 0);

            svgDocument.close();

            var newPdfDiv = new PdfDiv();
            var divWidth = 200f;
            var divHeight = 200f;
            newPdfDiv.setWidth(divWidth);
            newPdfDiv.setHeight(divHeight);
            var svgReader = new PdfReader(svgBaos.toByteArray());
            var page = outputWriter.getImportedPage(svgReader, 1);
            var newPdfDivContent = new ArrayList<Element>();
            newPdfDivContent.add(Image.getInstance(page));
            newPdfDiv.setContent(newPdfDivContent);
            outputDocument.add(newPdfDiv);
        }

        outputDocument.close();
    }

    private void drawSvg(PdfTemplate map, String resource,
                         int width, int height) throws IOException {

        var parser = XMLResourceDescriptor.getXMLParserClassName();
        var factory = new SAXSVGDocumentFactory(parser);

        var userAgent = new UserAgentAdapter();
        var loader = new DocumentLoader(userAgent);

        var ctx = new BridgeContext(userAgent, loader);
        ctx.setDynamicState(BridgeContext.DYNAMIC);

        var builder = new GVTBuilder();

        var g2d = new PdfGraphics2D(map, width, height);
        var city = factory.createSVGDocument(new File(resource).toURI()
                .toURL().toString());
        var mapGraphics = builder.build(ctx, city);
        mapGraphics.paint(g2d);
        g2d.dispose();
    }
}
