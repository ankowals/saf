package libs.libCore.modules;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class PdfCore {

    private Context scenarioCtx;

    public PdfCore() {
        this.scenarioCtx = ThreadContext.getContext("Scenario");
    }


    /**
     * read pdf file line by line and returns list of lines
     *
     * @param file File, file handle
     * @return List<String>
     */
    public List<String> readLines (File file) {

        PDDocument document = null;
        String[] lines = null;

        try {
            document = PDDocument.load(file);
            document.getClass();
            if ( ! document.isEncrypted() ) {
                PDFTextStripperByArea stripper = new PDFTextStripperByArea();
                stripper.setSortByPosition(true);
                PDFTextStripper tStripper = new PDFTextStripper();
                String pdfFileInText = tStripper.getText(document);

                // split by whitespace
                lines = pdfFileInText.split("\\r?\\n");
            } else {
                Log.error("Pdf file " + file + " is encrypted. Can't read it!");
            }
        } catch (IOException e) {
            Log.error("", e);
        }

        List<String> result = Arrays.asList(lines);

        return result;
    }

}