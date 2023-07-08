package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

// This class contains methods which, when used together, perform the entire Huffman Coding encoding and decoding process
 
public class HuffmanCoding {
    private String fileName;
    private ArrayList<CharFreq> sortedCharFreqList;
    private TreeNode huffmanRoot;
    private String[] encodings;

    /**
     * Constructor used by the driver, sets filename
     * @param f The file we want to encode
     */
	
    public HuffmanCoding( String f ) { 
        fileName = f; 
    }

    /**
     * Reads from filename character by character, and sets sortedCharFreqList
     * to a new ArrayList of CharFreq objects with frequency > 0, sorted by frequency
     */
	
    public void makeSortedList() {
        StdIn.setFile( fileName );

        int [] counting = new int [ 128 ];

        for ( int i = 0; i < 128; i++ ){

            counting[ i ] = 0;

        } 

        double total = 0;

        while ( StdIn.hasNextChar() ){

            char ch = StdIn.readChar();
            counting[ ch ]++; 
            total++;

        }

        sortedCharFreqList = new ArrayList<CharFreq>();

        for ( int j = 0; j < 128; j++ ){

            if ( counting[ j ] != 0 ){

                CharFreq characterFreq = new CharFreq( ( char ) j , counting[ j ] / total );

                sortedCharFreqList.add( characterFreq );
            }
        }
        if ( sortedCharFreqList.size() == 1 ){
            
            CharFreq characterFreq = new CharFreq( sortedCharFreqList.get( 0 ).getCharacter() , 0 );

        }
        Collections.sort( sortedCharFreqList );
}


    // Uses sortedCharFreqList to build a huffman coding tree, and stores its root in huffmanRoot
     
    public void makeTree() {

    ArrayList<CharFreq> list = sortedCharFreqList;
    Queue<TreeNode> source = new Queue<TreeNode>();
    Queue<TreeNode>target = new Queue<TreeNode>();

    for ( int i = 0; i < list.size(); i++ ){
        CharFreq current = list.get( i );
        TreeNode firstNode = new TreeNode ( current , null , null );
        source.enqueue(firstNode);
    }

    TreeNode nodeOne = source.dequeue();
    TreeNode nodeTwo = source.dequeue();

    double parentProbOccStart = nodeOne.getData().getProbOcc() + nodeTwo.getData().getProbOcc();
    CharFreq main = new CharFreq( null , parentProbOccStart );
    TreeNode parentStart = new TreeNode( main , nodeOne , nodeTwo );
    target.enqueue( parentStart );

    while( !source.isEmpty() || target.size() > 1 ){

        TreeNode left;
        TreeNode right;

        if ( target.isEmpty() ){

            left = source.dequeue();
            right = source.dequeue();

            double parentProbOcc = left.getData().getProbOcc() + right.getData().getProbOcc();
            CharFreq top = new CharFreq( null , parentProbOcc );
            TreeNode parent = new TreeNode( top , left , right );
            
            target.enqueue(parent);
        } else if ( source.isEmpty() ){

            left = target.dequeue();
            right = target.dequeue();

            double parentProbOcc = left.getData().getProbOcc() + right.getData().getProbOcc();
            CharFreq top = new CharFreq( null , parentProbOcc );
            TreeNode parent = new TreeNode( top , left , right );

            target.enqueue( parent );

        } else {

            CharFreq sourceDataFirst = source.peek().getData();
            CharFreq targetDataFirst = target.peek().getData();

            if ( sourceDataFirst.getProbOcc() <= targetDataFirst.getProbOcc() ){
                left = source.dequeue();

            } else {

                left = target.dequeue();
            }

            if ( source.isEmpty() ){

                right = target.dequeue();
            } else if ( target.isEmpty() ){

                right = source.dequeue();

            } else {

                CharFreq sourceDataSecond = source.peek().getData();
                CharFreq targetDataSecond = target.peek().getData();

                if ( sourceDataSecond.getProbOcc() <= targetDataSecond.getProbOcc() ){
                    right = source.dequeue();
                } else {

                    right = target.dequeue();
                }
            }

            double prob = left.getData().getProbOcc() + right.getData().getProbOcc();
            CharFreq top = new CharFreq( null , prob );
            TreeNode parent = new TreeNode( top , left , right );
            target.enqueue( parent );
        }

        TreeNode top = target.peek();
        huffmanRoot = top;
    }
}

    /**
     * Uses huffmanRoot to create a string array of size 128, where each index in the array contains 
     * that ASCII character's bitstring encoding.
     * Characters not present in the huffman coding tree should have their spots in the array left null.
     * Set encodings to this array.
     */
	
    public void makeEncodings() {

    String [] encode = new String[ 128 ];
    String toBinary = "";

    encode( encode , toBinary , huffmanRoot );
    encodings = encode;

    }
    private static void encode( String[] encode , String bits , TreeNode ptr ){
        if( ptr == null ){
            return;
        }
	    
        //checking leaf node
	    
        if( ptr.getLeft() == null && ptr.getRight() == null ){
            char letter = ptr.getData().getCharacter();
            int num = ( int ) letter;
            encode[ num ] = bits;
        }
	    
        // recursively adds to encoding
	    
        encode(encode, bits + "0", ptr.getLeft());
        encode(encode, bits + "1", ptr.getRight());
    }



    // Using encodings and filename, this method makes use of the writeBitString method to write the final encoding of 1's and 0's to the encoded file.
     
    public void encode( String encodedFile ) {
        StdIn.setFile( fileName );

        String encoding = "";

        while ( StdIn.hasNextChar() ){

            char letter = StdIn.readChar();
            String bit = encodings[ letter ];
            encoding = encoding + bit;

            writeBitString( encodedFile , encoding );
        }
    }
    

    /**
     * Writes a given string of 1's and 0's to the given file byte by byte and NOT as characters of 1 and 0 which take up 8 bits each
     * @param filename The file to write to (doesn't need to exist yet)
     * @param bitString The string of 1's and 0's to write to the file in bits
     */
	
    public static void writeBitString( String filename , String bitString ) {
        byte[] bytes = new byte[ bitString.length() / 8 + 1 ];
        int bytesIndex = 0, byteIndex = 0, currentByte = 0;

        // Pads the string with initial zeroes and then a one in order to bring its length to a multiple of 8. 
	// When reading, the 1 signifies the end of padding.
	    
        int padding = 8 - ( bitString.length() % 8 );
        String pad = "";
        for (int i = 0; i < padding - 1; i++) pad = pad + "0";
        pad = pad + "1";
        bitString = pad + bitString;

        // For every bit, adds it to the right spot in the corresponding byte and stores bytes in the array when finished
	    
        for ( char c : bitString.toCharArray() ) {
            if ( c != '1' && c != '0' ) {
                System.out.println( "Invalid characters in bitstring" );
                return;
            }

            if ( c == '1' ) currentByte += 1 << ( 7 - byteIndex );
            byteIndex++;
            
            if ( byteIndex == 8 ) {
                bytes[ bytesIndex ] = ( byte ) currentByte;
                bytesIndex++;
                currentByte = 0;
                byteIndex = 0;
            }
        }

        // array of bytes to the provided file
        try {
            FileOutputStream out = new FileOutputStream( filename );
            out.write( bytes );
            out.close();
        }
        catch( Exception e ) {
            System.err.println( "Error when writing to file!" );
        }
    }


    /**
     * Using a given encoded file name, this method makes use of the readBitString method 
     * to convert the file into a bit string, then decodes the bit string using the tree, and writes it to a decoded file.  
     * @param encodedFile The file which has already been encoded by encode()
     * @param decodedFile The name of the new file we want to decode into
     */
	
    public void decode( String encodedFile , String decodedFile ) {
        StdOut.setFile( decodedFile );

        String bits = readBitString( encodedFile );
        TreeNode ptr = huffmanRoot;

        for ( int i = 0; i < bits.length(); i++ ){

            char bit = bits.charAt(i);

            if ( ptr.getLeft() != null || ptr.getRight() != null ){
                if ( bit == '0' ){
                    ptr = ptr.getLeft();
                }
                if ( bit == '1' ){
                    ptr = ptr.getRight();
                }
            } else {

                char letter = ptr.getData().getCharacter();
                StdOut.print( letter );
                ptr = huffmanRoot;
                i--;

            }
        }

        if ( ptr.getLeft() == null && ptr.getRight() == null ){

            char letter = ptr.getData().getCharacter();
            StdOut.print( letter );
        }
    }


    /**
     * Reads a given file byte by byte, and returns a string of 1's and 0's representing the bits in the file
     * @param filename The encoded file to read from
     * @return String of 1's and 0's representing the bits in the file
     */
	
    public static String readBitString( String filename ) {
        String bitString = "";
        
        try {
            FileInputStream in = new FileInputStream( filename );
            File file = new File( filename );

            byte bytes[] = new byte[ ( int ) file.length() ];
            in.read( bytes );
            in.close();
            
            // For each byte read, converts to a binary string of length 8 and adds it to the bit string
		
            for ( byte b : bytes ) {
                bitString = bitString + 
                String.format ("%8s" , Integer.toBinaryString( b & 0xFF ) ).replace( ' ' , '0' );
            }

            // Detects the first 1 signifying the end of padding, then removes the first few characters, including  1
		
            for ( int i = 0; i < 8; i++ ) {
                if ( bitString.charAt(i) == '1' ) return bitString.substring( i + 1 );
            }
            
            return bitString.substring( 8 );
        }
        catch( Exception e ) {
            System.out.println( "Error while reading file!" );
            return "";
        }
    }

    // Getters used by the driver

    public String getFileName() { 
        return fileName; 
    }

    public ArrayList<CharFreq> getSortedCharFreqList() { 
        return sortedCharFreqList; 
    }

    public TreeNode getHuffmanRoot() { 
        return huffmanRoot; 
    }

    public String[] getEncodings() { 
        return encodings; 
    }
}
