package edu.grinnell.csc207.compression;

import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue;

/**
 * Node class containing data for the characters and their frequencies.
 * Also holds information about the type of node (internal or leaf) and the subchildren
 * so a Huffman tree can be constructed using a series of nodes.
 * Implements Comparable so nodes can be ordered in a priority queue.
 */
class Node implements Comparable<Node> {
    
    protected short character;
    
    protected int frequency;
    
    protected boolean isLeaf;
    
    protected Node left;
    
    protected Node right;
    
    /**
     * Constructs a new external leaf with a character value and frequency. Lets
     * left and right be null, as leaves have no children, and it is a leaf node.
     * @param character the numerical representation of the character
     * @param frequency the frequency at which this character occurs in the file
     */
    public Node(short character, int frequency) {
        this.character = character;
        this.frequency = frequency;
        left = null;
        right = null;
        isLeaf = true;
    }
    
    /**
     * Constructs a new internal node with a frequency and children. Leaves the
     * character blank, as it's not applicable, and it's not an external leaf.
     * @param frequency the combined frequency of the node's children
     * @param left the left node child
     * @param right the right node child
     */
    public Node(int frequency, Node left, Node right) {
        this.frequency = frequency;
        this.left = left;
        this.right = right;
        isLeaf = false;
    }
    
    /**
     * Compares the value of this node to another through their frequency values.
     * @param other another Node
     * @return the difference in frequency between the first and second objects
     */
    @Override
    public int compareTo(Node other) {
        return this.frequency - other.frequency;
    }
}

/**
 * Stores information about the shortened code for each character. Records the value
 * of the code and the length (number of bits in the code) for encoding purposes.
 */
class Code {
    
    protected short huffmanCode;
    
    protected short length;
    
    /**
     * Creates a new code with the value and length of the encoded data.
     * @param huffmanCode the value of the Huffman code representation
     * @param length the number of bits used in the code
     */
    public Code(short huffmanCode, short length) {
        this.huffmanCode = huffmanCode;
        this.length = length;
    }
}

/**
 * A HuffmanTree derives a space-efficient coding of a collection of byte
 * values.
 *
 * The Huffman tree encodes values in the range 0--255 which would normally
 * take 8 bits.  However, we also need to encode a special EOF character to
 * denote the end of a .grin file.  Thus, we need 9 bits to store each
 * byte value.  This is fine for file writing (modulo the need to write in
 * byte chunks to the file), but Java does not have a 9-bit data type.
 * Instead, we use the next larger primitive integral type, short, to store
 * our byte values.
 */
public class HuffmanTree {
    
    Node treeRoot;
    
    /* Short is the ASCII character from 8 bits of data reading,
     * Code is the Huffman code for encoding the data  */
    Map<Short, Code> huffmanCodes;
    
    public final short eof = 256;

    /**
     * Constructs a new HuffmanTree from a frequency map.
     * @param freqs a map from 9-bit values to frequencies.
     */
    public HuffmanTree (Map<Short, Integer> freqs) {
        PriorityQueue<Node> queue = makeQueue(freqs);
        treeRoot = constructTree(queue);
        huffmanCodes = new HashMap<>();
        recordCodes(treeRoot, (short) 0, (short) 0);
    }

    /**
     * Constructs a new HuffmanTree from the given file.
     * @param in the input file (as a BitInputStream)
     */
    public HuffmanTree (BitInputStream in) {
        treeRoot = readTree(in);
    }
    
    /**
     * Makes a priority queue out of the map given, turning each key/value into a node.
     * Also adds the EOF "character" with a value of 256
     * @param freqs the map of characters and their frequencies
     * @return priority queue of all character nodes
     */
    private PriorityQueue<Node> makeQueue(Map<Short, Integer> freqs) {
        PriorityQueue<Node> queue = new PriorityQueue<>();
        queue.add(new Node(eof, 1));
        for (Short key : freqs.keySet()) {
            queue.add(new Node(key, freqs.get(key)));
        }
        return queue;
    }
    
    /**
     * Creates a Huffman tree by de-queuing a priority queue, tracking the frequencies of
     * characters that occur and the frequencies of all sub nodes
     * @param queue priority queue sorting character nodes in ascending frequency order
     * @return the root node of the Huffman tree, containing all other nodes beneath
     */
    private Node constructTree(PriorityQueue<Node> queue) {
        Node left;
        Node right;
        while (queue.size() > 1) {
            left = queue.poll();
            right = queue.poll();
            queue.add(new Node(left.frequency + right.frequency, left, right));
        }
        return queue.poll();
    }
    
    /**
     * Initializes a map of every character and their Huffman code (both the value and length) 
     * @param root the node to start at
     * @param code the current code value based on the tree traversal
     * @param length the length of the Huffman code (number of bits used)
     */
    private void recordCodes(Node root, short code, short length) {
        if (root.isLeaf) {
            huffmanCodes.put(root.character, new Code(code, length));
        } else {
            length++;
            // Effectively adds a 0 at the end of the code (left branch)
            recordCodes(root.left, (short) (code << 1), length);
            // Effectively adds a 1 at the end of the code (right branch)
            recordCodes(root.right, (short) (code << 1 + 1), length);
        }
    }
    
    /**
     * Reads a serialized version of a Huffman tree from the input file and converts
     * it to a proper tree, progressing recursively in pre-order fashion.
     * @param in stream of bits to read from
     * @return root Node of the current tree hierarchy
     */
    private Node readTree(BitInputStream in) {
        int nextBit = in.readBit();
        Node newNode;
        if (nextBit == 1) {
            // 1 bit means internal node
            newNode = new Node(0, null, null);
            newNode.left = readTree(in);
            newNode.right = readTree(in);
        } else {
            // 0 bit means leaf with char
           newNode = new Node((short) in.readBits(9), 0);
        }
        return newNode;
    }

    /**
     * Writes this HuffmanTree to the given file as a stream of bits in a
     * serialized format.
     * @param out the output file as a BitOutputStream
     */
    public void serialize (BitOutputStream out) {
        writeNode(out, treeRoot);
    }
    
    /**
     * Writes the current node of the Huffman tree to the output stream, using the
     * serialization method defined in the instructions (see acknowledgements).
     * If the node is an inner leaf, a 1 and the node's children are printed, and if
     * it is a leaf, a 0 and the node value is printed.
     * @param out the output file stream
     * @param root the current node to serialize
     */
    private void writeNode (BitOutputStream out, Node root) {
        if (root.isLeaf) {
            // Write a 0 and the 8-digit "character" representation
            out.writeBit(0);
            out.writeBits(8, root.character);
        } else {
            // write a 1 and write the children nodes in pre-order
            out.writeBit(1);
            writeNode(out, root.left);
            writeNode(out, root.right);
        }
    }
   
    /**
     * Encodes the file given as a stream of bits into a compressed format
     * using this Huffman tree. The encoded values are written, bit-by-bit
     * to the given BitOuputStream.
     * @param in the file to compress.
     * @param out the file to write the compressed output to.
     */
    public void encode (BitInputStream in, BitOutputStream out) {
        // Read in 8 bits at a time and output their encoded form
        Code code;
        while (in.hasBits()) {
            code = huffmanCodes.get((short) in.readBits(8));
            out.writeBits(code.huffmanCode, code.length);
        }
        out.close();
    }

    /**
     * Decodes a stream of Huffman codes from a file given as a stream of
     * bits into their uncompressed form, saving the results to the given
     * output stream. Note that the EOF character is not written to out
     * because it is not a valid 8-bit chunk (it is 9 bits).
     * @param in the file to decompress.
     * @param out the file to write the decompressed output to.
     */
    public void decode (BitInputStream in, BitOutputStream out) {
        short character;
        while (true) {
            character = traceTree(in);
            if (character == eof) {
                break;
            } else {
                out.writeBits(character, 8);
            }
        }
        out.close();
    }
    
    /**
     * Finds the next ASCII character in the encoded file by taking either the left or
     * right branch of the tree (based on encoded input: left is 0, right is 1) until
     * a character is reached.
     * @param in the input file (as a BitInputStream)
     * @return the decoded character value (as a short to include eof)
     */
    private short traceTree(BitInputStream in) {
        int bit;
        Node root = treeRoot;
        while (true) {
            bit = in.readBit();
            root = (bit == 0 ? root.left : root.right);
            if (root.isLeaf) {
                return root.character;
            }
        }
    }
}
