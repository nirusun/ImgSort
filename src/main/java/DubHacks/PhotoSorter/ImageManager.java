package DubHacks.PhotoSorter;

/**
 * Image manager to store and cycle through images from
 * loaded folders.
 * 
 * @author Nathan Wong
 * @version 14 February 2015
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;

import edu.smu.tspell.wordnet.NounSynset;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;

public class ImageManager {
	private Map<String, List<Tag>>    imageBuffer;
	private Map<String, List<String>> categories;
	
	//private String path;
	
	private static final String IMAGE_REGEX = "(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|GIF|BMP)$";
	
	public ImageManager(String[] folderNames) {
		imageBuffer = new HashMap<String, List<Tag>>();
		categories  = new HashMap<String, List<String>>();
		
		for (String s : folderNames) {
			categories.put(s, new ArrayList<String>());
		}
	}
	
	public void sortImages(String path) {
		System.out.println("Sorting Start");
		loadImages(new File(path));
		loadCategories();
		
		write(path);
		
		System.out.println("Sorting End");
	}
	
	public void loadCategories() {
		System.out.println("Load Category Start");
		
		for (String folderName : categories.keySet()) {
			List<String> similarWords = getSimilarWords(folderName);
			
			for (String image : imageBuffer.keySet()) {
				if (listCompare(similarWords, imageBuffer.get(image)) && checkMap(categories, image)) {
					
					categories.get(folderName).add(image);
				} 
			}
		}
		
		System.out.println("Load Category End");
	}
	
	public boolean checkMap(Map<String, List<String>> map, String target) {
		for(String s : map.keySet()) {
			if (map.get(s).contains(target)) {
				return false;
			}
		}
		
		return true;
	}
	
	public void write(String path) {
		
		System.out.println("Writing Folder Start");
		
		String slash;
		if (System.getProperty("os.name").toLowerCase().equals("windows")) {
			slash = "\\";
		} else {
			slash = "/";
		}
		
		for (String folderName : categories.keySet()) {
			String newPath = path + slash + folderName;
			
			if (!Files.exists(Paths.get(newPath))) {
				if (!new File(newPath).mkdirs()) {
					System.err.println("Could not create new directory!");
					System.exit(0);
				}
			}
			
			try {
				for (String image : categories.get(folderName)) {
					System.out.println("Check Image: " + image);
					
					if (!Files.exists(Paths.get(newPath + slash + image))) { 
						Files.move(Paths.get(path + slash + image), Paths.get(newPath + slash + image));
					}
				}
				
				System.out.println("Move Success");
			} catch (IOException e) {
				System.err.println(e);
				System.exit(0);
			}
			
			System.out.println("Check Folder: " + folderName);
		}
		
		System.out.println("Writing Folder End");
	}
	
	private boolean listCompare(List<String> a, List<Tag> b) {
		System.out.println("Comparing Clarifai tags and synonyms");
		boolean result = false;
		
		for (Tag tag : b) {
			if (a.contains(tag.getName())) {
				result = true;
			}
		}
		
		return result;
	}
	
	
	/**
	 * Loads selected folder and adds contents to image buffer.
	 * Folder entries must be image files.
	 * 
	 * @param folder folder to be loaded to image buffer
	 */
	public void loadImages(File folder) {
		
		System.out.println("Load Images Start");
		
		Pattern pattern = Pattern.compile(IMAGE_REGEX);
		
		for (File fileEntry : folder.listFiles()) {
			String fileName = fileEntry.getName();
			
			System.out.println("Parsing " + fileName);
			
			Matcher matcher = pattern.matcher(fileName);
			
			if (fileEntry.isDirectory()) {
				loadImages(fileEntry);
			} else if (matcher.find()){
				imageBuffer.put(fileEntry.getName(), getTags(fileEntry.getAbsolutePath()));
			}
		}
		
		System.out.println("Load Images Start");
	}
	
	private List<Tag> getTags(String fileName) {
    	ClarifaiClient clarifai = new ClarifaiClient("AG82_y_1OVq8An-i6B9R4mLnqtZHhSZV0o_N5KHY", "c-VZHltVdeim_6XEVTVgYCTW3fPc95jH3grcXGfC");
    	List<RecognitionResult> results = 
    			clarifai.recognize(new RecognitionRequest(new File(fileName)));
    	return results.get(0).getTags();
	}
	
	public List<String> getSimilarWords(String word) {
		List<String> result = new ArrayList<String>();
		
		WordNetDatabase database = WordNetDatabase.getFileInstance(); 
		Synset[] synsets = database.getSynsets(word, SynsetType.NOUN); 
		
		for (int i = 0; i < synsets.length; i++) { 
		    NounSynset nounSynset = (NounSynset)(synsets[i]); 
		    NounSynset[] hyponyms = nounSynset.getHyponyms(); 
		    
		    result.add(nounSynset.getWordForms()[0]);
		    
		    for (NounSynset hyponym : hyponyms) {
		    	result.add(hyponym.getWordForms()[0]);
		    } 
		}
		
		return result;
	}
}