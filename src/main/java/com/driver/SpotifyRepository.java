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
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist = new Artist(name);
        artists.add(newArtist);
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
            Artist newArtist = new Artist(artistName);
            artists.add(newArtist);
            currentArtist=newArtist;
        }
        List<Album> albumList = new ArrayList<>();
        artistAlbumMap.put(currentArtist,albumList);

        Album newAlbum = new Album(title);
        if(!albums.contains(newAlbum))
            albums.add(newAlbum);
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
        //add song to album
        List<Song> songList = new ArrayList<>();
        songList.add(newSong);
        albumSongMap.put(currentAlbum,songList);
        //add song to songs
        if(!songs.contains(newSong))
            songs.add(newSong);
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
        if(!flag)throw new Exception();
        Playlist newPlaylist = new Playlist(title);
        List<User> userList = new ArrayList<>();
        userList.add(creator);
        playlistListenerMap.put(newPlaylist,userList);
        creatorPlaylistMap.put(creator,newPlaylist);
        playlists.add(newPlaylist);
        List<Song> li = new ArrayList<>();
        for(Song song : songs){
            if(song.getLength()==length)
                li.add(song);
        }
        playlistSongMap.put(newPlaylist,li);
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
        if(!flag)throw new Exception();
        Playlist newPlaylist = new Playlist(title);
        List<User> userList = new ArrayList<>();
        userList.add(creator);
        playlistListenerMap.put(newPlaylist,userList);
        playlists.add(newPlaylist);
        List<Song> li = new ArrayList<>();
        for(Song song : songs){
            String songTitle = song.getTitle();
            if(songTitles.contains(songTitle))
                li.add(song);
        }
        playlistSongMap.put(newPlaylist,li);
        return newPlaylist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //find if playlist exist
        Playlist searchedPlayList = null;
        for(Playlist playlist : playlists){
            if(playlist.getTitle().equals(playlistTitle)){
                searchedPlayList = playlist;
                break;
            }
        }
        if(searchedPlayList==null)throw new Exception("Playlist does not exist");
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

        // add user as listerns if not added
        List<User> listners = playlistListenerMap.get(searchedPlayList);
        boolean flag1 = false;

        for(User user : listners){
            if(user.getMobile().equals(mobile)){
                flag1=true;
                break;
            }
        }
        if(!flag1)listners.add(listner);
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
                List<Song> li = albumSongMap.get(key);
                for(Song song : li){
                    if(song==currentSong){
                        artistAlbum=key;
                        break;
                    }
                }
                if(artistAlbum!=null)break;
            }
            Artist likedArtist = null;
            for(Artist key : artistAlbumMap.keySet()){
                List<Album> li = artistAlbumMap.get(key);
                for(Album album : li){
                    if(album==artistAlbum){
                        likedArtist=key;
                        break;
                    }
                }
                if(likedArtist!=null)break;
            }
            if(likedArtist!=null){
                likedArtist.setLikes(likedArtist.getLikes()+1);
            }

        }
        return currentSong;
    }

    public String mostPopularArtist() {
        int max=0;
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
        int max=0;
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
