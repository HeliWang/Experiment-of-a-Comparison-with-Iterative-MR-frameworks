package jt;

import java.util.HashMap;
import java.util.Iterator;

public class LastFMUserR {
	public int userID = 0;
	public HashMap<Integer, Integer> artists;
	public HashMap<Integer, Integer> addtimes = new HashMap<Integer, Integer>();

	public LastFMUserR(int id, String data) {
		this.userID = id;
		String[] toAdd = data.split(" ");
		this.artists = new HashMap<Integer, Integer>();
		for (String s : toAdd)
			if (s.length() > 1) {
				String[] item = s.split(",");
				this.artists.put(Integer.valueOf(Integer.parseInt(item[0])),
						Integer.valueOf(Integer.parseInt(item[1])));
			}
	}

	public LastFMUserR(int id, String data, boolean times) {
		this.userID = id;
		String[] toAdd = data.split(" ");
		this.artists = new HashMap<Integer, Integer>();
		for (String s : toAdd)
			if (s.length() > 1) {
				String[] item = s.split(",");
				this.artists.put(Integer.valueOf(Integer.parseInt(item[0])),
						Integer.valueOf(Integer.parseInt(item[1])));
				this.addtimes.put(Integer.valueOf(Integer.parseInt(item[0])),
						Integer.valueOf(Integer.parseInt(item[2])));
			}
	}

	public LastFMUserR(int id, String data, int plays_threshold) {
		this.userID = id;
		String[] toAdd = data.split(" ");
		this.artists = new HashMap<Integer, Integer>();
		for (String s : toAdd)
			if (s.length() > 1) {
				String[] item = s.split(",");
				this.artists.put(Integer.valueOf(Integer.parseInt(item[0])),
						Integer.valueOf(Integer.parseInt(item[1])));
			}
	}

	public void add(LastFMUserR in) {
		HashMap<Integer, Integer> inArtists = in.artists;
		for(int artid : in.artists.keySet()){
            if(artists.containsKey(artid)){
                    int total = artists.get(artid) + inArtists.get(artid);
                    artists.put(artid, total);
            }else{
                    artists.put(artid, inArtists.get(artid));
            }
            
            if(addtimes.containsKey(artid)){
                    int total = addtimes.get(artid) + 1;;
                    addtimes.put(artid, total);
            }else{
                    addtimes.put(artid, 1);
            }
    }
	}

	public void addinred(LastFMUserR in) {
		HashMap<Integer, Integer> inArtists = in.artists;
        for(int artid : in.artists.keySet()){
                if(artists.containsKey(artid)){
                        int total = artists.get(artid) + inArtists.get(artid);
                        artists.put(artid, total);
                }else{
                        artists.put(artid, inArtists.get(artid));
                }
                
                if(addtimes.containsKey(artid)){
                        int total = addtimes.get(artid) + in.addtimes.get(artid);;
                        addtimes.put(artid, total);
                }else{
                        addtimes.put(artid, in.addtimes.get(artid));
                }
        }
	}

	public String getArtists(int threshold) {
		String out = new String();
        for(int artid : artists.keySet()){
                if(addtimes.get(artid) >= threshold){
                        int avg = artists.get(artid) / addtimes.get(artid);
                        out += artid + "," + avg + "," + addtimes.get(artid) + " ";
                }
        }
        return out;
	}

	public int MatchCount(LastFMUserR user) {
		int count = 0;
        for(int artid : user.artists.keySet()){
                if(this.artists.containsKey(artid)) count++;
        }

        return count;
	}

	public double SimpleDistance(LastFMUserR user) {
		int matchCount = MatchCount(user);
		int totalCount = user.artists.size() + this.artists.size() - matchCount;
		return (matchCount / totalCount);
	}
	// uses cosine distance - actually returns the square of the cosine of the angle between the
    // two high dimensional vectors.
    // Closer to 1 means higher similarity.
    public double ComplexDistance(LastFMUserR user) {
            
            double dotProduct = 0.0;
            double magOne = 0.0;
            double magTwo = 0.0;

            HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>(artists);
            for(int artid : user.artists.keySet()){
                    if(this.artists.containsKey(artid)){
                            dotProduct += this.artists.get(artid) * user.artists.get(artid);
                            temp.remove(artid);
                    }else{
                            magOne = user.artists.get(artid) * user.artists.get(artid);
                    }
            }
            for(int artid : temp.keySet()){
                    magTwo = temp.get(artid) * temp.get(artid);
            }

            return (dotProduct*dotProduct) / (1+magOne*magTwo);
    }
    
    @Override
    public String toString() {
            String out = userID + ":";
            for(int artid : artists.keySet()){
                    out += artid + "," + artists.get(artid) + " ";
            }
            return out;
    }
    
    public String artistsString() {
            String out = new String();
            for(int artid : artists.keySet()){
                    out += artid + "," + artists.get(artid) + " ";
            }
            return out;
    }

	public class LastFMArtist {
		public int artistID;
		public int plays;

		public LastFMArtist(String data) {
			String[] items = data.split(",");
			this.artistID = Integer.parseInt(items[0]);
			this.plays = Integer.parseInt(items[1]);
		}

		public String toString() {
			return this.artistID + "," + this.plays;
		}

		public int hashCode() {
			return (this.artistID % 37);
		}

		public boolean equals(Object o) {
			return (((LastFMArtist) o).artistID == this.artistID);
		}
	}
}
