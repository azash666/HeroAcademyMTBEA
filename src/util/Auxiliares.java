package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import action.Action;
import ai.myTest.MyNtbea2.LModel;
import game.GameState;
import model.HaMap;
import model.Unit;


public class Auxiliares {

	public static <E> void imprime(List<E> lista) {
		System.out.println("List = [");
		for (E obj: lista) {
			System.out.println("    "+obj);
		}
		System.out.println("]");
	}
	
	public static <E> void imprime(Set<E> conjunto) {
		System.out.println("Set = [");
		for (E obj: conjunto) {
			System.out.println("    "+obj);
		}
		System.out.println("]");
	}
	
	public static void imprime(HaMap mapa) {
		System.out.println("Mapa = [");
		for (int y=0; y<mapa.height; y++) {
			System.out.print("[");
			for (int x=0; x<mapa.width; x++) {
				
				System.out.print(mapa.squareAt(x, y) + "    \t");
			}
			System.out.println("]");
		}
		System.out.println("]");
	}

	public static void imprime(Unit[][] units) {
		System.out.println("Units = [");
		for (int y=0; y<units[0].length; y++) {
			System.out.print("[");
			for (int x=0; x<units.length; x++) {
				
				System.out.print(units[x][y] + "    \t");
			}
			System.out.println("]");
		}
		System.out.println("]");
	}
	

	public static void imprime(Object[] acciones) {imprime(acciones, null);}

	public static void imprime(Object[] acciones, String outputFile) {
		if(outputFile!=null) {
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(outputFile, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    PrintWriter printWriter = new PrintWriter(fileWriter);
		    
		    printWriter.print("vector: [");
			for(Object obj: acciones) {
				printWriter.print(obj);
			}
			printWriter.print("]");
			
		    printWriter.close();
		}else{
			System.out.print("vector: [");
			for(Object obj: acciones) {
				System.out.print(obj);
			}
			System.out.println("]");
		}
	}
	
	public static <E, P> void imprime(Map<E[], P> mapa) {imprime(mapa, null);}

	public static <E, P> void imprime(Map<E[], P> mapa, String outputFile) {
		if(outputFile!=null) {
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(outputFile, true);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    PrintWriter printWriter = new PrintWriter(fileWriter);
		    
		    printWriter.println("Mapa = [");
			Set<E[]> keys = mapa.keySet();
			for(E[] key : keys) {
				imprime(key, outputFile);
				printWriter.println(" --> "+mapa.get(key));
			}
			printWriter.println("]");
			
		    printWriter.close();
		}else{
			System.out.println("Mapa = [");
			Set<E[]> keys = mapa.keySet();
			for(E[] key : keys) {
				imprime(key);
				System.out.println(" --> "+mapa.get(key));
			}
			System.out.println("]");
		}
	}
	
	public static void imprime(LModel lmodel) {
		System.out.println("IMPRIMIENDO LMODEL:");
		System.out.println("Individuales:");
		for (int i=0; i<lmodel.size; i++) {
			System.out.println("Bandido "+i);
			for (String act: lmodel.individual.get(i).keySet()) {
				System.out.println(act + " --> "+lmodel.individual.get(i).get(act));
			}
		}
	}
}

