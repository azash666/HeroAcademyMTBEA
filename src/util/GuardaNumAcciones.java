package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class GuardaNumAcciones {
	private List<Integer> numAcciones[] = new List[50];
	private String archivo;
	
	public GuardaNumAcciones(String archivo) {
		this.archivo = archivo;
		for(int i=0; i<50; i++) numAcciones[i]= new LinkedList<Integer>();
		File file = new File(archivo); 
		Scanner sc = null;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) { e.printStackTrace();}
		
		int turno=0;
		while (sc.hasNextLine()) {
			
			String[] vector = sc.nextLine().split("\t");
			List<Integer> lista = new LinkedList<Integer>();
			for (String c : vector) {
				if (!c.equals("")) lista.add(Integer.parseInt(c));
			}
			numAcciones[turno++] = lista;
		}
		sc.close();
	}
	
	public void add(int turno, int cantidad) {
		if (turno>100) return;
		int turnoReal = (turno-1)/2;
		numAcciones[turnoReal].add(cantidad);
	}
	
	public void close() {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(this.archivo);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    PrintWriter printWriter = new PrintWriter(fileWriter);
	    printWriter.print("");
	    int longitud = numAcciones[0].size();
	    for (int i=0; i<50; i++) {
	    	String aux = "";
		    for (int j=0; j< longitud; j++) {
		    	
		    		aux += j<numAcciones[i].size() ? numAcciones[i].get(j) : 0;
		    	if (j<longitud-1) aux+="\t";
		    }
		    printWriter.println(aux);
	    }
	    printWriter.close();
	}
}
