package edu.grinnell.csc207.compression;

import java.io.IOException;
import java.util.Map;

/**
 * The driver for the Grin compression program.
 */
public class Grin {
    /**
     * Decodes the .grin file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * @param infile the file to decode
     * @param outfile the file to ouptut to
     * @throws Exception if error setting up files
     */
    public static void decode (String infile, String outfile) throws Exception {
        BitInputStream in = new BitInputStream(infile);
        BitOutputStream out = new BitOutputStream(outfile);
        
        if (in.readBits(32) != 1846) {
            throw new IllegalArgumentException();
        }
        
        HuffmanTree hTree = new HuffmanTree(in);
        hTree.decode(in, out);
    }

    /**
     * Creates a mapping from 8-bit sequences to number-of-occurrences of
     * those sequences in the given file. To do this, read the file using a
     * BitInputStream, consuming 8 bits at a time.
     * @param file the file to read
     * @return a frequency map for the given file
     */
    public static Map<Short, Integer> createFrequencyMap (String file) {
        // TODO: fill me in!
        return null;
    }

    /**
     * Encodes the given file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * @param infile the file to encode.
     * @param outfile the file to write the output to.
     * @throws IOException if error setting up files
     */
    public static void encode(String infile, String outfile) throws IOException{
        // TODO: fill me in!
    }

    /**
     * The entry point to the program.
     * @param args the command-line arguments.
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java Grin <encode|decode> <infile> <outfile>");
            System.exit(0);
        }
        
        try {
            switch (args[0]) {
                case "encode":
                    encode(args[1], args[2]);
                    break;
                case "decode":
                    decode(args[1], args[2]);
                    break;
                default:
                    System.out.println("Usage: java Grin <encode|decode> <infile> <outfile>");
            }
        } catch (Exception e) {
            System.out.println("Error in file setup. Please enter valid files of proper types.");
        }
    }
}
