package DubHacks.PhotoSorter;

import java.util.Arrays;

public class ImgSort {
    public static void main(String[] args) {
    	if(args.length < 2) {
    		System.err.println("Usage: folderPath tag1, tag2, tag3, ...");
    		System.exit(1);
    	}
    	
    	System.setProperty("wordnet.database.dir", "/home/nathan/WordNet-3.0/dict/");
    	
    	try {
    		String path = args[0];
    		String[] folders = Arrays.copyOfRange(args, 1, args.length);
    		
	    	ImageManager imageManager = new ImageManager(folders);
			imageManager.sortImages(path);
			
    	} catch(Exception e) {
    		System.err.println("Invalid path name");
    		System.exit(1);
    	}
    }
}
