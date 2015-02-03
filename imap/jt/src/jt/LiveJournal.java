package jt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


public class LiveJournal {
	public static int acount = 0;
	public static int zcount = 0;
	public static String getnewid(int id){

		String outid = "";
		for(int i = 0; i<acount ; i++){
			outid += "A";
		}
		for(int i = 0; i<zcount ; i++){
			outid += "Z";
		}
		outid += id;
		
		return outid;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String input = args[0];
		String output = args[1];
		String output2 = args[2];
		
		BufferedReader br = new BufferedReader(new FileReader(input));
		BufferedWriter bw = new BufferedWriter(new FileWriter(output));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(output2));
		
		Random rand = new Random();
		int currentid = 0;
		ArrayList<Integer> links = new ArrayList<Integer>();
		int errn = 0;
		int predest = 0;
		
		while(br.ready()){
			String line = br.readLine();
			if(line.indexOf("#") != -1) continue;
			
			String front = line.substring(0, line.indexOf("\t"));
			acount = front.length() - front.replace("A", "").length();
			zcount = front.length() - front.replace("Z", "").length();
			
			line = line.replaceAll("A","");
			line = line.replaceAll("Z","");
			
			int src = Integer.parseInt(line.substring(0, line.indexOf("\t")));
			int des = Integer.parseInt(line.substring(line.indexOf("\t") + 1));
			if(src > currentid){
				while(src - currentid > 1){
					int id = currentid++;
					System.out.println("gap: " + src + "\t" + currentid);
					bw.write(getnewid(id) + "\t" + getnewid(rand.nextInt(currentid)) + "\n");
					bw2.write(getnewid(id) + "\t1\n");
//					currentid++;
				}
				bw.write(getnewid(currentid)+ "\t");
				predest = 0;
				for(int dest : links){
					while(dest - predest > 0 && errn > 0){
						bw.write(getnewid(predest) + " ");
						predest++;
						errn--;
					}
					bw.write(getnewid(dest) + " ");
				}
				bw.write("\n");
				bw2.write(getnewid(currentid) + "\t1\n");
				links.clear();
				
			}else if(src < currentid){
				src = currentid;
				System.out.println("error " + src);
				errn += 1;
//				System.exit(-1);
			}
			//3부터 적용할수 있게 
			
			links.add(des);
			currentid = src;
		}
		bw.write(getnewid(currentid) + "\t");
		predest = 0;
		for(int dest : links){
			while(dest - predest > 0 && errn > 0){
				bw.write(getnewid(predest) + " ");
				predest++;
				errn--;
			}
			bw.write(getnewid(dest) + " ");
		}
		bw.write("\n");
		bw2.write(getnewid(currentid) + "\t1\n");
		
		br.close();
		bw.close();
		bw2.close();
	}

}
