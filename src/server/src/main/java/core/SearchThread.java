package main.java.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import main.java.dfs.ChordMessageInterface;
import main.java.dfs.DFS;
import main.java.model.Artist;
import main.java.model.Music;
import main.java.model.Song;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SearchThread implements Runnable {
	String search;
	List<Music> searchResult;
	Long time;
	int page;
	DFS.FileJson file;
	DFS dfs;


	public SearchThread(int page, DFS.FileJson file, DFS dfs, String search) {
		this.page = page;
		this.file = file;
		this.dfs = dfs;
		this.search = search;
	}

	public List<Music> getResults(){
		return searchResult;
	}

	@Override
	public void run() {
		List<Music> music = null;
		Long pageGuid = this.file.getPages().get(page).getGUID();
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		Type type = new TypeToken<List<Music>>() {}.getType();
		try {
			ChordMessageInterface peer = dfs.getChord().locateSuccessor(pageGuid);
			String searchResultString = peer.search(pageGuid,search);
			System.out.println("SEARCH TEST: " + searchResultString);
			List<Music> a = gson.fromJson(searchResultString, type);
			music.addAll(a);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.searchResult = music;
		System.out.println("Thread " + this.page);
	}

//	@Override
//	public void run() {
//		List<Music> music = null;
//		Long pageGuid = this.file.getPages().get(page).getGUID();
//		Gson gson = new GsonBuilder().setPrettyPrinting().create();
//		Type type = new TypeToken<List<Music>>() {}.getType();
//		try {
//			ChordMessageInterface succ = dfs.getChord().locateSuccessor(pageGuid);
//			System.out.println(Long.toString(succ.getId()) + "/repository/" + Long.toString(pageGuid));
//			FileReader reader = new FileReader(Long.toString(succ.getId()) + "/repository/" + Long.toString(pageGuid));
//			music = gson.fromJson(reader, type);
//			reader.close();
//		}catch(FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		List<Music> searchedMusicList = new ArrayList<>();
//
//		Stream<Music> stream = music.stream()
//				.filter(m -> m.getSong().getTitle().toLowerCase().contains(search.toLowerCase())
//						|| m.getArtist().getName().toLowerCase().contains(search.toLowerCase())
//						|| m.getArtist().getTerms().toLowerCase().contains(search.toLowerCase()));
//
//		searchedMusicList = stream.collect(Collectors.toList());
//
//		this.searchResult = searchedMusicList;
//		System.out.println("Thread " + this.page);
//	}
}