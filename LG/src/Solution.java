
public class Solution {


    public static void main(String[] args){


        int point = 0;

        int[] healths ={200, 120, 150};
        int[][] items = {{1000,600},{400,500},{300,100}};

        int[] answer ={};



                for(int i = 0; i<items.length; i++) {
                    int count =0;
                    for (int j = 0; j < healths.length; j++) {

                        if (healths[j] - items[i][1] < 100) {
                            count++;
                        }
                    }
                    if(count == healths.length) {
                        items[i][0] = 0 ;
                        items[i][1] = 0;
                    }

                }



        }












        }


    }


