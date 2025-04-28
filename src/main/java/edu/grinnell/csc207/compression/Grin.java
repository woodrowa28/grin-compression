package edu.grinnell.csc207.compression;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

/**
 * The driver for the Grin compression program.
 */
public class Grin {
    /**
     * Decodes the .grin file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * @param infile the file to decode
     * @param outfile the file to ouptut to
     * @throws IOException if error parsing files
     * @throws IllegalArgumentException if decoding file is not a .grin
     */
    public static void decode(String infile, String outfile) throws IOException,
            IllegalArgumentException {
        BitInputStream in = new BitInputStream(infile);
        BitOutputStream out = new BitOutputStream(outfile);
        
        if (in.readBits(32) != 1846) {
            throw new IllegalArgumentException();
        }
        
        HuffmanTree hTree = new HuffmanTree(in);
        hTree.decode(in, out);
        in.close();
        out.close();
    }

    /**
     * Creates a mapping from 8-bit sequences to number-of-occurrences of
     * those sequences in the given file. To do this, read the file using a
     * BitInputStream, consuming 8 bits at a time.
     * @param file the file to read
     * @return a frequency map for the given file
     * @throws IOException upon file parsing error
     */
    public static Map<Short, Integer> createFrequencyMap(String file) 
            throws IOException {
        Map<Short, Integer> frequencies = new HashMap<>();
        BitInputStream in = new BitInputStream(file);
        
        short character;
        while (in.hasBits()) {
            character = (short) in.readBits(8);
            if (frequencies.containsKey(character)) {
                frequencies.put(character, frequencies.get(character) + 1);
            } else {
                frequencies.put(character, 1);
            }
        }
        // Add EOF char
        frequencies.put((short) 256, 1);
        
        in.close();
        return frequencies;
    }

    /**
     * Encodes the given file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * @param infile the file to encode.
     * @param outfile the file to write the output to.
     * @throws IOException if error setting up files
     */
    public static void encode(String infile, String outfile) throws IOException {
        Map<Short, Integer> frequencies = createFrequencyMap(infile);
        BitInputStream in = new BitInputStream(infile);
        BitOutputStream out = new BitOutputStream(outfile);
        HuffmanTree hTree = new HuffmanTree(frequencies);
        
        out.writeBits(1846, 32);
        hTree.serialize(out);
        hTree.encode(in, out);
        
        in.close();
        out.close();
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
        } catch (IOException e) {
            System.out.println("Error parsing file. Please enter valid files of proper types.");
            System.out.println(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Not a valid file type to decode. Must be a .grin file");
            System.out.println(e.getMessage());
        }
    }
}
