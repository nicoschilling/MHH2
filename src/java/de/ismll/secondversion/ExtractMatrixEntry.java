package de.ismll.secondversion;

import java.io.File;
import java.io.IOException;

import de.ismll.bootstrap.Parameter;
import de.ismll.mhh.io.Parser;
import de.ismll.storage.FileStorageTarget;
import de.ismll.table.Matrix;

public class ExtractMatrixEntry implements Runnable{

        @Parameter(cmdline="file")
        private File matrixFile;

        @Parameter(cmdline="target")
        private File target;

        @Parameter(cmdline="col")
        private int col;

        @Parameter(cmdline="row")
        private int row;

        public File getMatrixFile() {
                return matrixFile;
        }

        public void setMatrixFile(File matrixFile) {
                this.matrixFile = matrixFile;
        }

        public int getCol() {
                return col;
        }

        public void setCol(int col) {
                this.col = col;
        }

        public int getRow() {
                return row;
        }

        public void setRow(int row) {
                this.row = row;
        }

        @Override
        public void run() {
                Matrix readAnnotations;
                try {
                        readAnnotations = Parser.readAnnotations(matrixFile, 50);
                } catch (IOException e) {
                        throw new RuntimeException("Failed to load file from " + matrixFile, e);
                }

                int f = (int) readAnnotations.get(row, col);

                try {
                        FileStorageTarget.store(target, f);
                } catch (IOException e) {
                        throw new RuntimeException("Failed to write content to file " + target, e);
                }

        }

        public File getTarget() {
                return target;
        }

        public void setTarget(File target) {
                this.target = target;
        }
}
