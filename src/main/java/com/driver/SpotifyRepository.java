package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User newUser = new User(name,mobile);
        users.add(newUser);
        userPlaylistMap.put(newUser, new ArrayList<>());
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist = new Artist(name);
        artists.add(newArtist);
        artistAlbumMap.put(newArtist, new ArrayList<>());
        return newArtist;
    }

    public Album createAlbum(String title, String artistName) {

        boolean flag=false;
        Artist currentArtist = null;
        for(Artist artist: artists){
            if(artist.getName().equals(artistName))
            {
                currentArtist = artist;
                flag=true;
                break;
            }
        }
        if(!flag) {
            createArtist(artistName);
        }
        Album newAlbum = new Album(title);
        albums.add(newAlbum);
        artistAlbumMap.get(currentArtist).add(newAlbum);
        albumSongMap.put(newAlbum, new ArrayList<>());
        return newAlbum;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        boolean flag=false;
        Album currentAlbum = null;
        for(Album album: albums){
            if(album.getTitle().equals(albumName))
            {
                currentAlbum = album;
                flag=true;
                break;
            }
        }
        if(!flag)throw new Exception("Album does not exist");

        Song newSong = new Song(title, length);
        newSong.setLikes(0);
        songs.add(newSong);

        //add song to album
        albumSongMap.get(currentAlbum).add(newSong);
        songLikeMap.put(newSong, new ArrayList<>());

        return newSong;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        boolean flag=false;
        User creator = null;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                flag=true;
                creator=user;
                break;
            }
        }
        if(!flag)throw new Exception("User does not exist");

        Playlist newPlaylist = new Playlist(title);
        playlists.add(newPlaylist);

        playlistListenerMap.put(newPlaylist,new ArrayList<>());
        playlistSongMap.put(newPlaylist, new ArrayList<>());

        List<Song> li = playlistSongMap.get(newPlaylist);
        for(Song song : songs){
            if(song.getLength()==length)
                li.add(song);
        }

        creatorPlaylistMap.put(creator,newPlaylist);
        playlistListenerMap.get(newPlaylist).add(creator);
        userPlaylistMap.get(creator).add(newPlaylist);

        return newPlaylist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {

        boolean flag=false;
        User creator = null;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                creator=user;
                flag=true;
                break;
            }
        }
        if(!flag)throw new Exception("User does not exist");

        Playlist newPlaylist = new Playlist(title);
        playlists.add(newPlaylist);

        playlistSongMap.put(newPlaylist, new ArrayList<>());
        playlistListenerMap.put(newPlaylist, new ArrayList<>());

        List<Song> li = playlistSongMap.get(newPlaylist);
        for(Song song : songs){
            String songTitle = song.getTitle();
            if(songTitles.contains(songTitle))
                li.add(song);
        }

        playlistListenerMap.get(newPlaylist).add(creator);
        creatorPlaylistMap.put(creator,newPlaylist);
        userPlaylistMap.get(creator).add(newPlaylist);

        return newPlaylist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //find if user already exist
        boolean flag = false;
        User listner = null;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                listner = user;
                flag=true;
                break;
            }
        }
        if(!flag)throw new Exception("User does not exist");

        //find if playlist exist
        Playlist searchedPlayList = null;
        for(Playlist playlist : playlists){
            if(playlist.getTitle().equals(playlistTitle)){
                searchedPlayList = playlist;
                break;
            }
        }
        if(searchedPlayList==null)throw new Exception("Playlist does not exist");


        // add user as listerns if not added
        if(creatorPlaylistMap.containsKey(listner) && creatorPlaylistMap.get(listner)==searchedPlayList ||
            playlistListenerMap.get(searchedPlayList).contains(listner)){
            return  searchedPlayList;
        }
        playlistListenerMap.get(searchedPlayList).add(listner);

        if(!userPlaylistMap.get(listner).contains(searchedPlayList)) {
            userPlaylistMap.get(listner).add(searchedPlayList);
        }
        return searchedPlayList;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        //check if user exist
        boolean flag = false;
        User currentUser = null;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                currentUser = user;
                flag=true;
                break;
            }
        }
        if(!flag)throw new Exception("User does not exist");

        //check if song exist
        boolean flag1 = false;
        Song currentSong = null;
        for(Song song : songs){
            if(song.getTitle().equals(songTitle)){
                currentSong = song;
                flag1=true;
                break;
            }
        }
        if(!flag1)throw new Exception("Song does not exist");

        //User like the current song if not done already

        List<User> userLiked = songLikeMap.get(currentSong);
        boolean liked = false;
        for(User user : userLiked){
            if(user.getMobile().equals(mobile)){
                liked=true;
                break;
            }
        }
        if(!liked){
            userLiked.add(currentUser);
            currentSong.setLikes(currentSong.getLikes()+1);
            //corresponding artist liked automatically
            Album artistAlbum = null;
            for(Album key : albumSongMap.keySet()){
                if(albumSongMap.get(key).contains(currentSong)){
                    for(Artist artist : artistAlbumMap.keySet()){
                        if(artistAlbumMap.get(artist).contains(key)){
                            artist.setLikes(artist.getLikes()+1);
                            break;
                        }
                    }
                }
            }
        }
        return currentSong;
    }

    public String mostPopularArtist() {
        int max=-1;
        String artistName="";
        for(Artist artist:artists){
            if(artist.getLikes()>max){
                max= artist.getLikes();
                artistName = artist.getName();
            }
        }
        return artistName;
    }

    public String mostPopularSong() {
        int max=-1;
        String songName="";
        for(Song song:songs){
            if(song.getLikes()>max){
                max= song.getLikes();
                songName = song.getTitle();
            }
        }
        return songName;
    }
}
