package jt;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class LastFMUserR {
	public int userID = 0;
	public HashMap<Integer, Integer> artists;
	public HashMap<Integer, Integer> addtimes = new HashMap();

	public LastFMUserR(int id, String data) {
		this.userID = id;
		String[] toAdd = data.split(" ");
		this.artists = new HashMap();
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
		this.artists = new HashMap();
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
		this.artists = new HashMap();
		for (String s : toAdd)
			if (s.length() > 1) {
				String[] item = s.split(",");
				this.artists.put(Integer.valueOf(Integer.parseInt(item[0])),
						Integer.valueOf(Integer.parseInt(item[1])));
			}
	}

	public void add(LastFMUserR in) {
		HashMap inArtists = in.artists;
		for (Iterator i$ = in.artists.keySet().iterator(); i$.hasNext();) {
			int artid = ((Integer) i$.next()).intValue();
			if (this.artists.containsKey(Integer.valueOf(artid))) {
				int total = ((Integer) this.artists.get(Integer.valueOf(artid)))
						.intValue()
						+ ((Integer) inArtists.get(Integer.valueOf(artid)))
								.intValue();
				this.artists
						.put(Integer.valueOf(artid), Integer.valueOf(total));
			} else {
				this.artists.put(Integer.valueOf(artid),
						(Integer) inArtists.get(Integer.valueOf(artid)));
			}

			if (this.addtimes.containsKey(Integer.valueOf(artid))) {
				int total = ((Integer) this.addtimes
						.get(Integer.valueOf(artid))).intValue() + 1;
				this.addtimes.put(Integer.valueOf(artid),
						Integer.valueOf(total));
			} else {
				this.addtimes.put(Integer.valueOf(artid), Integer.valueOf(1));
			}
		}
	}

	public void addinred(LastFMUserR in) {
		HashMap inArtists = in.artists;
		for (Iterator i$ = in.artists.keySet().iterator(); i$.hasNext();) {
			int artid = ((Integer) i$.next()).intValue();
			if (this.artists.containsKey(Integer.valueOf(artid))) {
				int total = ((Integer) this.artists.get(Integer.valueOf(artid)))
						.intValue()
						+ ((Integer) inArtists.get(Integer.valueOf(artid)))
								.intValue();
				this.artists
						.put(Integer.valueOf(artid), Integer.valueOf(total));
			} else {
				this.artists.put(Integer.valueOf(artid),
						(Integer) inArtists.get(Integer.valueOf(artid)));
			}

			if (this.addtimes.containsKey(Integer.valueOf(artid))) {
				int total = ((Integer) this.addtimes
						.get(Integer.valueOf(artid))).intValue()
						+ ((Integer) in.addtimes.get(Integer.valueOf(artid)))
								.intValue();
				this.addtimes.put(Integer.valueOf(artid),
						Integer.valueOf(total));
			} else {
				this.addtimes.put(Integer.valueOf(artid),
						in.addtimes.get(Integer.valueOf(artid)));
			}
		}
	}

	public String getArtists(int threshold) {
		String out = new String();
		for (Iterator i$ = this.artists.keySet().iterator(); i$.hasNext();) {
			int artid = ((Integer) i$.next()).intValue();
			if (((Integer) this.addtimes.get(Integer.valueOf(artid)))
					.intValue() >= threshold) {
				int avg = ((Integer) this.artists.get(Integer.valueOf(artid)))
						.intValue()
						/ ((Integer) this.addtimes.get(Integer.valueOf(artid)))
								.intValue();
				out = out + artid + "," + avg + ","
						+ this.addtimes.get(Integer.valueOf(artid)) + " ";
			}
		}
		return out;
	}

	public int MatchCount(LastFMUserR user) {
		int count = 0;
		for (Iterator i$ = user.artists.keySet().iterator(); i$.hasNext();) {
			int artid = ((Integer) i$.next()).intValue();
			if (this.artists.containsKey(Integer.valueOf(artid)))
				++count;
		}

		return count;
	}

	public double SimpleDistance(LastFMUserR user) {
		int matchCount = MatchCount(user);
		int totalCount = user.artists.size() + this.artists.size() - matchCount;
		return (matchCount / totalCount);
	}

	public double ComplexDistance(LastFMUserR user) {
		double dotProduct = 0.0D;
		double magOne = 0.0D;
		double magTwo = 0.0D;

		HashMap temp = new HashMap(this.artists);
		for (Iterator i$ = user.artists.keySet().iterator(); i$.hasNext();) {
			int artid = ((Integer) i$.next()).intValue();
			if (this.artists.containsKey(Integer.valueOf(artid))) {
				dotProduct += ((Integer) this.artists.get(Integer
						.valueOf(artid))).intValue()
						* ((Integer) user.artists.get(Integer.valueOf(artid)))
								.intValue();
				temp.remove(Integer.valueOf(artid));
			} else {
				magOne = ((Integer) user.artists.get(Integer.valueOf(artid)))
						.intValue()
						* ((Integer) user.artists.get(Integer.valueOf(artid)))
								.intValue();
			}
		}
		for (Iterator i$ = temp.keySet().iterator(); i$.hasNext();) {
			int artid = ((Integer) i$.next()).intValue();
			magTwo = ((Integer) temp.get(Integer.valueOf(artid))).intValue()
					* ((Integer) temp.get(Integer.valueOf(artid))).intValue();
		}

		return (dotProduct * dotProduct / (1.0D + magOne * magTwo));
	}

	public String toString() {
		String out = this.userID + ":";
		for (Iterator i$ = this.artists.keySet().iterator(); i$.hasNext();) {
			int artid = ((Integer) i$.next()).intValue();
			out = out + artid + "," + this.artists.get(Integer.valueOf(artid))
					+ " ";
		}
		return out;
	}

	public String artistsString() {
		String out = new String();
		for (Iterator i$ = this.artists.keySet().iterator(); i$.hasNext();) {
			int artid = ((Integer) i$.next()).intValue();
			out = out + artid + "," + this.artists.get(Integer.valueOf(artid))
					+ " ";
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
