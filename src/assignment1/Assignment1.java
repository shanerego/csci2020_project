/*
 * Program: The Purpose of the Program is meant to determine the accuracy of a 
 * filter through emails.
 *
 * Completed by: Rolando Agullano (100622368) and Shane Rego (100623789)
 * 
 * Note: I sampled code from the user kinejohnsrud on GitHub, merely as a guideline.
 */

package assignment1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class Assignment1 {

    private static Window primaryStage;
    //maps for the words and numbers used
    static TreeMap<String, Word> trainSpamFreq = new TreeMap<String, Word>();
    static TreeMap<String, Word> trainHamFreq = new TreeMap<String, Word>();
    public static int userChoice = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        FileInputStream in = null;
        FileOutputStream out = null;

        String line = null;
        String[] val = {};

        if (Integer.parseInt(args[0]) == 1) {
            userChoice = 1;
        } else {
            userChoice = 0;
        }
        Bayes();
    }

    public static void Bayes() throws IOException {
        String line = null;
        String[] val = {};
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setInitialDirectory(new File("."));
            File mainDirectory = directoryChooser.showDialog(primaryStage);
        } catch (IllegalStateException e) {
            System.out.println("Possible Error: Cannot Compute.");
        }

        //output for all files in folder
        final File folder = new File("C:\\Users\\rolan\\Desktop\\data\\test\\ham");

        for (final File fileEntry : folder.listFiles()) {
            try {
                // FileReader reads text files in the default encoding.

                FileReader fileReader = new FileReader(fileEntry);

                // Always wrap FileReader in BufferedReader.
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                Trainfile newTrain = new Trainfile();
                TestFile newTest = new TestFile();

                if (userChoice == 1) {
                    while ((line = bufferedReader.readLine()) != null) {
                        newTrain.train(line);
                    }
                } else {
                    while ((line = bufferedReader.readLine()) != null) {
                        newTest.test(line);
                    }
                }

                // Always close files.
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                System.out.println(
                        "Unable to open file '" + fileEntry + "'");
            }
            System.out.println(fileEntry.getName());
        }
    }

    public static class Trainfile {
        
        //Test will count the amount of spam and ham and determine probability
        public void train(String input) throws IOException {
            BufferedReader in = new BufferedReader(new FileReader(input));
            int sCount = 0, hCount = 0;
            String line = in.readLine();
            
            while (line != null) {
                if (!line.equals("")) {
                    String type = line.split("\t")[0];
                    String sms = line.split("\t")[1];
                    for (String word : sms.split(" ")) {
                        word = word.replaceAll("\\W", "");
                        word = word.toLowerCase();
                        Word w = null;
                        if (trainHamFreq.containsKey(word)) {
                            w = (Word) trainHamFreq.get(word);
                        } else {

                            trainHamFreq.put(word, w);
                        }
                        if (type.equals("ham")) {
                            w.countHam();
                            hCount++;
                        } else if (type.equals("spam")) {
                            w.countSpam();
                            sCount++;
                        }
                    }
                }
                line = in.readLine();
            }
            in.close();

            for (String key : trainHamFreq.keySet()) {
                trainHamFreq.get(key).calculateProbability(sCount, hCount);
            }
        }
    }

    public static class TestFile {

        //declare variables
        private String filename;
        private double spamProbability;
        private String actualClass;

        public TestFile() {
        };   
        
        //Test will count the amount of spam and ham and determine probability
        public void test(String input) throws IOException {
            BufferedReader in = new BufferedReader(new FileReader(input));
            String line = in.readLine();
            int sCount = 0;
            int hCount = 0;
            
            while (line != null) {
                if (!line.equals("")) {
                    String type = line.split("\t")[0];
                    String sms = line.split("\t")[1];
                    for (String word : sms.split(" ")) {
                        word = word.replaceAll("\\W", "");
                        word = word.toLowerCase();
                        Word w = null;
                        if (trainHamFreq.containsKey(word)) {
                            w = (Word) trainHamFreq.get(word);
                        } else {

                            trainHamFreq.put(word, w);
                        }
                        if (type.equals("ham")) {
                            w.countHam();
                            hCount++;
                        } else if (type.equals("spam")) {
                            w.countSpam();
                            sCount++;
                        }
                    }
                }
                line = in.readLine();
            }
            in.close();

            for (String key : trainHamFreq.keySet()) {
                trainHamFreq.get(key).calculateProbability(sCount, hCount);
            }
        }
        
        /***********
         * 
         * @param TestFile
         * @param spamProbability
         * @param actualClass 
         */
        public TestFile(String filename,
                double spamProbability,
                String actualClass) {
            this.filename = filename;
            this.spamProbability = spamProbability;
            this.actualClass = actualClass;
        }

        public String getFilename() {
            return this.filename;
        }

        public double getSpamProbability() {
            return this.spamProbability;
        }

        public String getSpamProbRounded() {
            DecimalFormat df = new DecimalFormat("0.00000");
            return df.format(this.spamProbability);
        }

        public String getActualClass() {
            return this.actualClass;
        }

        public void setFilename(String value) {
            this.filename = value;
        }

        public void setSpamProbability(double val) {
            this.spamProbability = val;
        }

        public void setActualClass(String value) {
            this.actualClass = value;
        }
    }
    
    /*********
     * class will define the initiation of "word"
     */
    public class Word {
        
        //declare variable
        String word;
        int spamCount, hamCount;
        double sRate, hRate, pSpam;

        public Word(String word) {
            this.word = word;
            spamCount = 0;
            hamCount = 0;
            sRate = 0.0;
            hRate = 0.0;
            pSpam = 0.0;
        }

        public void countSpam() { spamCount++; }
        public void countHam() { hamCount++; }

        public void calculateProbability(int totSpam, int totHam) {
            sRate = spamCount / (float) totSpam;
            hRate = hamCount / (float) totHam;

            if ((sRate + hRate) > 0) {
                pSpam = sRate / (sRate + hRate);
            }
            if (pSpam < 0.01) {
                pSpam = 0.01;
            } else if (pSpam > 0.99) {
                pSpam = 0.99;
            }
        }

        public String getWord() { return word; }
        public double getSpamRate() { return sRate; }
        public double getHamRate() { return hRate; }
        public void setHamRate(float hRate) { this.hRate = hRate; }
        public double getSpamProbability() { return pSpam; }
        public void setProbOfSpam(float pSpam) { this.pSpam = pSpam; }
    }
}
