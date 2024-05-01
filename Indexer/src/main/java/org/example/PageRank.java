package org.example;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PageRank {
    public static Map<String, Double> pageRankMap;

    public static void start() {
//        int[][] matrixData = {{1,1,0}, {1,0,0},{1,1,1}};
//        double[]matrixData2 = { 1.0,2,3};
//        double[]n = { 1.0,2,3};
        //double[] rank=start(matrixData,3,1);
        pageRankMap=new HashMap<>();
        WebGraphBuilder builder = new WebGraphBuilder();
        builder.buildGraphFromHTMLFiles("C:\\Users\\abdom\\Downloads\\Second Term-20240213T104511Z-001\\projects\\SearchEngineJava\\Indexer\\src\\main\\resources\\sample");
        //builder.printAdjacencyMatrix();
        double[] rank=start(builder.adjacencyMatrix,builder.adjacencyMatrix.length,.8);
        System.out.println("the page rank");
        for(String key: builder.urlIndexMap.keySet()){
            // builder.urlIndexMap.get(key) is index of certain url
            pageRankMap.put(key,1e10*rank[builder.urlIndexMap.get(key)]);
        }

    }
    public static double[] start(int[][] graph, int size, double beta){
        double[] rank_new=new double[size];
        Arrays.fill(rank_new, 1.0/size);
        double[] rank_prev=new double[size];
        Arrays.fill(rank_prev, 1.0/size);
        double[] c=new double[size];
        Arrays.fill(c, (1 - beta) /size );
        double[][] pathesProbabilityMatrix=new double[size][size];
        for (int i = 0; i < size; i++) {
            double sum=0;
            for (int j = 0; j < size; j++) {
                sum+=graph[j][i];
            }
            if(sum==0){
                //System.out.println("SUM equals 0");
                sum=1;
            }
            for (int j = 0; j < size; j++) {
                pathesProbabilityMatrix[j][i]=graph[j][i]/sum;
            }

        }
        while (true){
            rank_new=mutiplyMatrix(pathesProbabilityMatrix,rank_prev,size,beta);
            addVectors(rank_new,c,size);
            if(equals(rank_prev[0],rank_new[0])){
                break;
            }
            rank_prev=rank_new;
        }
        return rank_new;
    }
    public static double[] mutiplyMatrix(double[][] m, double[] n, int size,double beta){
        double[] result= new double[size];
        for (int i = 0; i <size ; i++) {
            double res=0;
            for (int j = 0; j <size ; j++) {
                res+=m[i][j]*n[j];
            }
            result[i]=res*beta;
        }
        return result;
    }
    public static void addVectors(double[] m, double[] n,int size){
        double[] result= new double[size];
        for (int i = 0; i <size ; i++) {
            m[i]+=n[i];
        }
    }
    public static boolean equals(double v1,double v2 ){
        double epslon=1e-25;
        return Math.abs(v1-v2)<epslon;
    }



}

