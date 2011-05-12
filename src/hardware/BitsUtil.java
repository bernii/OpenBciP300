package hardware;

import java.util.BitSet;

public class BitsUtil {
	public byte[] toByte(String bitRepresentation){
		BitSet bs = new BitSet(bitRepresentation.length());
		for(int i=0;i<bitRepresentation.length();i++){
			if(bitRepresentation.charAt(i)=='1')
				bs.set(i);
		}
		return toByteArray(bs);
	}
	
	// Returns a byte array of at least length 1.
    // The most significant bit in the result is guaranteed not to be a 1
    // (since BitSet does not support sign extension).
    // The byte-ordering of the result is big-endian which means the most significant bit is in element 0.
    // The bit at index 0 of the bit set is assumed to be the least significant bit.
    public byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length()/8+1];
        for (int i=0; i<bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length-i/8-1] |= 1<<(i%8);
            }
        }
        return bytes;
    }
    
    // Returns a bitset containing the values in bytes.
    // The byte-ordering of bytes must be big-endian which means the most significant bit is in element 0.
    public BitSet fromByteArray(byte[] byt) {
    	byte[] bytes = new byte[byt.length];
    	for (int i = 0; i < bytes.length; i++) {
			bytes[i] = byt[byt.length-1-i];
		}
        BitSet bits = new BitSet();
        for (int i=0; i<bytes.length*8; i++) {
            if ((bytes[bytes.length-i/8-1]&(1<<(i%8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }
    
    public void printByteArray(byte[] bytes){
    	System.out.println("Read from right to left ! ");
    	BitSet bs = fromByteArray(bytes);
    	short bit = 0 ;
    	for(int i = 0 ; i<bs.length();i++){
    		if(i%8==0)
    			System.out.println(" ");
    		bit = 0 ;
    		if(bs.get(i))
    			bit = 1 ;
    		System.out.print(" "+bit);
    	}
    }
}
