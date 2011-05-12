package general;

import java.util.Arrays;
import java.util.Stack;

public class Utils {
	public static int[] intersect ( int [] a , int [] b )
	{
		Arrays.sort(a);
		Arrays.sort(b);
		
		Stack<Integer> stack = new Stack<Integer>();
		int[] c;
		int i=0,j=0;
		while(i<a.length&&j<b.length)
		{
			if(a[ i ]==b[ j ]){stack.push( new Integer( a[ i ] ) );i++;j++;}
			else if(a[ i ] >b[ j ])j++;
			else if(a[ i ] <b[ j ])i++;
		}
		c=new int[stack.size()];
		for(int k=c.length-1;k>=0;k--)
			c[ k ]=((Integer)stack.pop()).intValue ();
		return c;
	}
}
