/*
  Copyright 2006 by Sean Luke and George Mason University
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/

package sim.app.neuseriver;

import sim.app.neuseriver.pointSource;
import sim.engine.*;
import sim.field.grid.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.*;

public class Trade implements Steppable
    {
    private static final long serialVersionUID = 1;
    /*Initialization flag */
    private boolean initialized = false;
    /*Number of point source agents */
    private int numberPoints = 19;
    /*Array of point source agents */
    private pointSource[] points;
    private int stepNumber = 0;

    /*Steppable simulation method */
    public void step(SimState state)
        {
    	//Initialization call (only performed once)
    	if (this.initialized == false) {
    		initialize();
    	}
    	//printPoints();
    	
        //randomTrade();
        bilateralTrade();
        //printPoints();
        this.stepNumber++;
        System.out.println();
        System.out.println("*********************************");
        System.out.println("Step Number = " + this.stepNumber);
        System.out.println("*********************************");
        System.out.println();        
        
        }
    
    /*Initialization method */
    public void initialize() {
    	set_pointSources();
    	this.initialized = true;
    }
    
    /*Instantiation of point source agents */
    public void set_pointSources() {
    	this.points = new pointSource[this.numberPoints];
    	String [] pointAttributes = new String[this.numberPoints];
    	try {
    		File f = new File("pointInput.txt");
    		Scanner s = new Scanner(f);
    		int k = 0;
    		while(s.hasNextLine()) {
    			pointAttributes[k] = s.nextLine();
        		k++;
        	}
    		s.close();
    	} catch (FileNotFoundException e) {
    		System.out.println("File Not Found Exception!!!!!!!");
    	}
    	for (int i = 0; i < this.numberPoints; i++) {
			Scanner lineScan = new Scanner(pointAttributes[i]);
			double transportFactor = 0.0;
			double flowRate = 0.0;
			double initialConc = 0.0;
			double dischargeLimit = 0.0;
			double costX2 = 0.0;
			double costX1 = 0.0;
			double costConstant = 0.0;
			double rangeMin = 0.0;
			double rangeMax = 0.0;
			int place = 0;
			while (lineScan.hasNext()) {
				if (place == 0) {
					transportFactor = Double.parseDouble(lineScan.next());
				}
				if (place == 1) {
					flowRate = Double.parseDouble(lineScan.next());
				}
				if (place == 2) {
					initialConc = Double.parseDouble(lineScan.next());
				}
				if (place == 3) {
					dischargeLimit = Double.parseDouble(lineScan.next());
				}
				if (place == 4) {
					costX2 = Double.parseDouble(lineScan.next());	
				}
				if (place == 5) {
					costX1 = Double.parseDouble(lineScan.next());	
				}
				if (place == 6) {
					costConstant = Double.parseDouble(lineScan.next());	
				}
				if (place == 7) {
					rangeMin = Double.parseDouble(lineScan.next());	
				}
				if (place == 8) {
					rangeMax = Double.parseDouble(lineScan.next());	
				}
				place++;
			}
			lineScan.close();
			//pointSource(int index, double transportFactor, double flowRate, double initialConc, double dischargeLimit, double costX2, double costX1, double costConstant, double rangeMin, double rangeMax)
			pointSource objectSet = new pointSource(i, transportFactor, flowRate, initialConc, dischargeLimit, costX2, costX1, costConstant, rangeMin, rangeMax);
			this.points[i] = objectSet;
		}
    }
    
    /*Iterate through the point sources and determine the number
     * of buyers in the current time step
     */
    public int countBuyers() {
    	int buyerCount = 0;
    	for(int i = 0; i < points.length; i++) {
    		if(points[i].isBuyer()) {
    			buyerCount++;
    		}
    	}
    	return buyerCount;
    }
    
    /*Iterate through the point sources and determine the number
     * of sellers in the current time step
     */
    public int countSellers() {
    	int sellerCount = 0;
    	for(int i = 0; i < points.length; i++) {
    		if(points[i].isSeller()) {
    			sellerCount++;
    		}
    	}
    	return sellerCount;
    }
    
    /*Iterates through the point sources and fills the buyerIndex
     * with the index values of all buyers in the current time step
     */
    public void fillBuyerIndex(int[] buyerArray) {
    	int currentBuyerIndex = 0;
    	for(int i = 0; i < points.length; i++) {
    		if(points[i].isBuyer()) {
    			buyerArray[currentBuyerIndex] = points[i].get_Index();
    			currentBuyerIndex++;
    		}
    	}
    }
    
    /*Iterates through the point sources and fills the sellerIndex
     * with the index values of all sellers in the current time step
     */
    public void fillSellerIndex(int[] sellerArray) {
    	int currentSellerIndex = 0;
    	for(int i = 0; i < points.length; i++) {
    		if(points[i].isSeller()) {
    			sellerArray[currentSellerIndex] = points[i].get_Index();
    			currentSellerIndex++;
    		}
    	}
    }
    
    /* Performs a bilateral trade scenario between a number
     * of sellers and a number of buyers for nitrogen offsets
     */
    public void bilateralTrade() {
    	int buyerCount = countBuyers();
    	int sellerCount = countSellers();
    	int[] buyerIndex = new int[buyerCount];
    	int[] sellerIndex = new int[sellerCount];
    	fillBuyerIndex(buyerIndex);
    	//Method is needed here for calculating buyers' first WTP
    	updateWTP1();
    	//Calculate offsets needed
    	calculateBuyerNeed();
    	fillSellerIndex(sellerIndex);
    	//Need to update WTA here
    	updateWTA();
    	//Need to determine maximum number of offsets that sellers can sell
    	calculateSellerSupply();
    	
    	//Functions for buyers
    	boolean[] buyerCovered = new boolean[buyerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		buyerCovered[i] = false;
    	}
    	int[] buyersOrdered = new int[buyerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		int currentMaxIndex = 1000;
    		double currentMax = -10000000.0;
    		for (int j = 0; j < buyerCount; j++) {
    			if (this.points[buyerIndex[j]].get_wtp1() >= currentMax && buyerCovered[j] == false) {
    				currentMaxIndex = buyerIndex[j];
    				currentMax = this.points[buyerIndex[j]].get_wtp1();
    			}
    		}
    		buyersOrdered[i] = currentMaxIndex;
    		for (int k = 0; k < buyerCount; k++) {
				if (buyerIndex[k] == currentMaxIndex) {
					buyerCovered[k] = true;
				}
			}
    	}
    	for (int i = 0; i < buyerCount; i++) {
    		//System.out.println("Buyer Index = " + buyersOrdered[i] + " WTP = " + this.points[buyersOrdered[i]].get_wtp1());
    	}
    	//Corresponds to buyerIndex array
    	double[] buyerInitialNeed = new double[buyerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		buyerInitialNeed[i] = (-1) * (this.points[buyerIndex[i]].get_dischargeDifference());
    	}
    	
    	//Functions for sellers
    	boolean[] sellerCovered = new boolean[sellerCount];
    	for (int i = 0; i < sellerCount; i++) {
    		sellerCovered[i] = false;
    	}
    	int[] sellersOrdered = new int[sellerCount];
    	for (int i = 0; i < sellerCount; i++) {
    		int currentMinIndex = 1000;
    		double currentMin = 10000000.0;
    		for (int j = 0; j < sellerCount; j++) {
    			if (this.points[sellerIndex[j]].get_wta() <= currentMin && sellerCovered[j] == false) {
    				currentMinIndex = sellerIndex[j];
    				currentMin = this.points[sellerIndex[j]].get_wta();
    			}
    		}
    		sellersOrdered[i] = currentMinIndex;
    		for (int k = 0; k < sellerCount; k++) {
				if (sellerIndex[k] == currentMinIndex) {
					sellerCovered[k] = true;
				}
			}
    	}
    	for (int i = 0; i < sellerCount; i++) {
    		//System.out.println("Seller Index = " + sellersOrdered[i] + " WTA = " + this.points[sellersOrdered[i]].get_wta());
    	}
    	System.out.println("Exchange #1 ********************************");
    	exchangeOffsets1(buyersOrdered, sellersOrdered);
    	update_points();
    	//Calculate WTP2 for original buyers
    	//Assign a value of zero to WTP2 if no further offsets needed 
    	for (int i = 0; i < buyerCount; i++) {
    		double creditsStillNeeded = (-1) * this.points[buyerIndex[i]].get_dischargeDifference();
    		if (creditsStillNeeded <= 0) {
    			this.points[buyerIndex[i]].set_wtp2(0);
    		} else {
    			double wtp2 = ((this.points[buyerIndex[i]].get_wtp1() * buyerInitialNeed[i]) / creditsStillNeeded);
    			this.points[buyerIndex[i]].set_wtp2(wtp2);
    		}
    	}
    	
    	boolean[] buyerCovered2 = new boolean[buyerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		buyerCovered2[i] = false;
    	}
    	int[] buyersOrdered2 = new int[buyerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		int currentMaxIndex = 1000;
    		double currentMax = -10000000.0;
    		for (int j = 0; j < buyerCount; j++) {
    			if (this.points[buyerIndex[j]].get_wtp2() >= currentMax && buyerCovered2[j] == false) {
    				currentMaxIndex = buyerIndex[j];
    				currentMax = this.points[buyerIndex[j]].get_wtp2();
    			}
    		}
    		buyersOrdered2[i] = currentMaxIndex;
    		for (int k = 0; k < buyerCount; k++) {
				if (buyerIndex[k] == currentMaxIndex) {
					buyerCovered2[k] = true;
				}
			}
    	}
    	for (int i = 0; i < buyerCount; i++) {
    		//System.out.println("Buyer Index = " + buyersOrdered2[i] + " WTP = " + this.points[buyersOrdered2[i]].get_wtp2());
    	}
    	calculateBuyerNeed();
    	updateWTA();
    	calculateSellerSupply();
    	
    	boolean[] sellerCovered2 = new boolean[sellerCount];
    	for (int i = 0; i < sellerCount; i++) {
    		sellerCovered2[i] = false;
    	}
    	int[] sellersOrdered2 = new int[sellerCount];
    	for (int i = 0; i < sellerCount; i++) {
    		int currentMinIndex = 1000;
    		double currentMin = 10000000.0;
    		for (int j = 0; j < sellerCount; j++) {
    			if (this.points[sellerIndex[j]].get_wta() <= currentMin && sellerCovered2[j] == false) {
    				currentMinIndex = sellerIndex[j];
    				currentMin = this.points[sellerIndex[j]].get_wta();
    			}
    		}
    		sellersOrdered2[i] = currentMinIndex;
    		for (int k = 0; k < sellerCount; k++) {
				if (sellerIndex[k] == currentMinIndex) {
					sellerCovered2[k] = true;
				}
			}
    	}
    	for (int i = 0; i < sellerCount; i++) {
    		//System.out.println("Seller Index = " + sellersOrdered2[i] + " WTA = " + this.points[sellersOrdered2[i]].get_wta());
    	}
    	System.out.println("Exchange #2 ********************************");
    	exchangeOffsets2(buyersOrdered2, sellersOrdered2);
    	update_points();
    	
    }
    
    /* Performs a random trade scenario between a number
     * of sellers and a number of buyers for nitrogen offsets
     */
    public void randomTrade() {
    	update_points();
    	int buyerCount = countBuyers();
    	int sellerCount = countSellers();
    	int[] buyerIndex = new int[buyerCount];
    	int[] sellerIndex = new int[sellerCount];
    	fillBuyerIndex(buyerIndex);
    	//Method is needed here for calculating buyers' first WTP
    	updateWTP1();
    	//Calculate offsets needed
    	calculateBuyerNeed();
    	fillSellerIndex(sellerIndex);
    	//Need to update WTA here
    	updateWTA();
    	//Need to determine maximum number of offsets that sellers can sell
    	calculateSellerSupply();
    	fillSellerIndex(sellerIndex);
    	Integer[] buyerIndex2 = new Integer[buyerCount];
    	Integer[] sellerIndex2 = new Integer[sellerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		buyerIndex2[i] = buyerIndex[i];
    	}
    	for (int i = 0; i < sellerCount; i++) {
    		sellerIndex2[i] = sellerIndex[i];
    	}
    	List<Integer> template = Arrays.asList(buyerIndex2);
    	List<Integer> items = new ArrayList<Integer>(template);
    	Collections.shuffle(items);
    	int[] buyersOrdered = new int[buyerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		buyersOrdered[i] = items.get(i);
    	}
    	List<Integer> template2 = Arrays.asList(sellerIndex2);
    	List<Integer> items2 = new ArrayList<Integer>(template2);
    	Collections.shuffle(items2);
    	int[] sellersOrdered = new int[sellerCount];
    	for (int i = 0; i < sellerCount; i++) {
    		sellersOrdered[i] = items2.get(i);
    	}
    	System.out.println(items);
    	System.out.println(items2);
    	//Corresponds to buyerIndex array
    	for (int i = 0; i < buyerCount; i++) {
    		System.out.println("Buyer Index = " + buyersOrdered[i] + " WTP = " + this.points[buyersOrdered[i]].get_wtp1());
    	}
    	for (int i = 0; i < sellerCount; i++) {
    		System.out.println("Seller Index = " + sellersOrdered[i] + " WTA = " + this.points[sellersOrdered[i]].get_wta());
    	}
    	double[] buyerInitialNeed = new double[buyerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		buyerInitialNeed[i] = (-1) * (this.points[buyerIndex[i]].get_dischargeDifference());
    	}
    	exchangeOffsets1(buyersOrdered, sellersOrdered);
    	update_points();
    	
    	//Calculate WTP2 for original buyers
    	//Assign a value of zero to WTP2 if no further offsets needed 
    	for (int i = 0; i < buyerCount; i++) {
    		double creditsStillNeeded = (-1) * this.points[buyerIndex[i]].get_dischargeDifference();
    		if (creditsStillNeeded <= 0) {
    			this.points[buyerIndex[i]].set_wtp2(0);
    		} else {
    			double wtp2 = ((this.points[buyerIndex[i]].get_wtp1() * buyerInitialNeed[i]) / creditsStillNeeded);
    			this.points[buyerIndex[i]].set_wtp2(wtp2);
    		}
    	}
    	
    	Integer[] buyerIndex3 = new Integer[buyerCount];
    	Integer[] sellerIndex3 = new Integer[sellerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		buyerIndex3[i] = buyerIndex[i];
    	}
    	for (int i = 0; i < sellerCount; i++) {
    		sellerIndex3[i] = sellerIndex[i];
    	}
    	List<Integer> template3 = Arrays.asList(buyerIndex3);
    	List<Integer> items3 = new ArrayList<Integer>(template3);
    	Collections.shuffle(items3);
    	int[] buyersOrdered2 = new int[buyerCount];
    	for (int i = 0; i < buyerCount; i++) {
    		buyersOrdered2[i] = items3.get(i);
    	}
    	List<Integer> template4 = Arrays.asList(sellerIndex3);
    	List<Integer> items4 = new ArrayList<Integer>(template4);
    	Collections.shuffle(items4);
    	int[] sellersOrdered2 = new int[sellerCount];
    	for (int i = 0; i < sellerCount; i++) {
    		sellersOrdered2[i] = items4.get(i);
    	}
    	System.out.println(items3);
    	System.out.println(items4);
    	for (int i = 0; i < buyerCount; i++) {
    		System.out.println("Buyer Index = " + buyersOrdered2[i] + " WTP = " + this.points[buyersOrdered2[i]].get_wtp2());
    	}
    	for (int i = 0; i < sellerCount; i++) {
    		System.out.println("Seller Index = " + sellersOrdered2[i] + " WTA = " + this.points[sellersOrdered2[i]].get_wta());
    	}
    	exchangeOffsets2(buyersOrdered2, sellersOrdered2);
    	update_points();
    	
    }
    
    /* This method iterates through an ordered set of buyers
     * and sellers and performs necessary exchanges based on
     * seller's willingness to accept, seller's available offsets,
     * buyer's willingness to pay, and buyer's necessary offsets
     */
    public void exchangeOffsets1(int[] buyers, int[] sellers) {
    	int totalExchanges = 0;
    	if (buyers.length < sellers.length) {
    		totalExchanges = buyers.length;
    	} else {
    		totalExchanges = sellers.length;
    	}
    	for (int i = 0; i < totalExchanges; i++) {
    		int sellerIndex = sellers[i];
    		int buyerIndex = buyers[i];
    		double seller_wta = 0.0;
    		double buyer_wtp = 0.0;
    		double seller_cap = 0.0;
    		double buyer_need = 0.0;
    		//Exchange Amount in units of lbs of Nest per year (N at Estuary)
    		double exchangeAmount = 0.0;
    		seller_wta = this.points[sellerIndex].get_wta();
    		seller_cap = this.points[sellerIndex].get_transportFactor() * (this.points[sellerIndex].get_maximumOffsetsForSale() - this.points[sellerIndex].get_offsetsSold());
    		buyer_wtp = this.points[buyerIndex].get_wtp1();
    		buyer_need = this.points[buyerIndex].get_transportFactor() * (this.points[buyerIndex].get_dischargeDifference() * -1);
    		if (seller_wta <= buyer_wtp) {
    			if (seller_cap >= buyer_need) {
    				exchangeAmount = buyer_need;
    			} else {
    				exchangeAmount = seller_cap;
    			}
    			this.points[sellerIndex].add_offsetsSold(exchangeAmount * (1/this.points[sellerIndex].get_transportFactor()));
    			this.points[buyerIndex].add_offsetsBought(exchangeAmount * (1/this.points[buyerIndex].get_transportFactor()));
    			System.out.println("----------- An Exchange Has Been Made");
    			System.out.println("Seller WTA = " + seller_wta + " Buyer WTP = " + buyer_wtp);
    		} else {
    			System.out.println("No Exchange Made -----------");
    			System.out.println("Seller WTA = " + seller_wta + " Buyer WTP = " + buyer_wtp);
    		}
    		System.out.println("Exchange = " + exchangeAmount);
    	}
    }
    
    public void exchangeOffsets2(int[] buyers, int[] sellers) {
    	int totalExchanges = 0;
    	if (buyers.length < sellers.length) {
    		totalExchanges = buyers.length;
    	} else {
    		totalExchanges = sellers.length;
    	}
    	for (int i = 0; i < totalExchanges; i++) {
    		int sellerIndex = sellers[i];
    		int buyerIndex = buyers[i];
    		double seller_wta = 0.0;
    		double buyer_wtp = 0.0;
    		double seller_cap = 0.0;
    		double buyer_need = 0.0;
    		//Exchange Amount in units of lbs of Nest per year (N at Estuary)
    		double exchangeAmount = 0.0;
    		seller_wta = this.points[sellerIndex].get_wta();
    		seller_cap = this.points[sellerIndex].get_transportFactor() * (this.points[sellerIndex].get_maximumOffsetsForSale() - this.points[sellerIndex].get_offsetsSold());
    		buyer_wtp = this.points[buyerIndex].get_wtp2();
    		buyer_need = this.points[buyerIndex].get_transportFactor() * (this.points[buyerIndex].get_dischargeDifference() * -1);
    		if (buyer_need <= 0) {
    			buyer_need = 0;
    		}
    		if (seller_wta <= buyer_wtp) {
    			if (seller_cap >= buyer_need) {
    				exchangeAmount = buyer_need;
    			} else {
    				exchangeAmount = seller_cap;
    			}
    			this.points[sellerIndex].add_offsetsSold(exchangeAmount * (1/this.points[sellerIndex].get_transportFactor()));
    			this.points[buyerIndex].add_offsetsBought(exchangeAmount * (1/this.points[buyerIndex].get_transportFactor()));
    			System.out.println("----------- An Exchange Has Been Made");
    			System.out.println("Seller WTA = " + seller_wta + " Buyer WTP = " + buyer_wtp);
    		} else {
    			System.out.println("No Exchange Made -----------");
    			System.out.println("Seller WTA = " + seller_wta + " Buyer WTP = " + buyer_wtp);
    		}
    		System.out.println("Exchange = " + exchangeAmount);
    	}
    }
    
    /* This method updates the treatment amount, current discharge, current limit, and discharge
     * difference value for every point source
     * in the model. Should be used after exchanges have been made.
     */
    public void update_points() {
    	for (int i = 0; i < this.numberPoints; i++) {
    		this.points[i].update_treatmentAmount();
    		this.points[i].updateTreatmentConc();
    		this.points[i].update_currentDischarge();
    		this.points[i].update_currentLimit();
    		this.points[i].update_dischargeDifference();
    		this.points[i].update_monthlyDischarge();
    		this.points[i].update_monthlyDischargeEst();
    		this.points[i].maxTreatmentOffsetsCalc();
    	}
    }
    
    /* This method prints out the point sources and a selected
     * number of attributes.
     */
    public void printPoints() {
    	System.out.println("Point Sources");
    	System.out.println("---------------------------------------");
    	for (int i = 0; i < this.numberPoints; i++) {
    		System.out.println("Index = " + this.points[i].get_Index());
    		System.out.println("WTP1 = " + this.points[i].get_wtp1());
    		System.out.println("WTP2 = " + this.points[i].get_wtp2());
    		System.out.println("WTA = " + this.points[i].get_wta());
    		System.out.println("Offsets Sold = " + this.points[i].get_offsetsSold());
    		System.out.println("Offsets Bought = " + this.points[i].get_offsetsBought());
    		System.out.println("Annual Discharge Limit = " + this.points[i].get_currentLimit());
    		System.out.println("Current Monthly Discharge Est = " + this.points[i].get_monthlyDischargeEst());
    		
    	}
    }
    
    //Method is needed here for calculating buyers' first WTP
    public void updateWTP1() {
    	for (int i = 0; i < this.numberPoints; i++) {
    		this.points[i].set_wtp1(this.points[i].calcCurrentWTP());
    	}
    }
    
  //Method is needed here for calculating sellers' WTA
    public void updateWTA() {
    	for (int i = 0; i < this.numberPoints; i++) {
    		this.points[i].set_wta(this.points[i].calcCurrentWTP());
    	}
    }
    
    //Method is used to count the number of offsets needed by all buyers
    public double calculateBuyerNeed() {
    	double need = 0;
    	for (int i = 0; i < this.numberPoints; i++) {
    		if (this.points[i].get_dischargeDifference() < 0) {
    			need += (-1) * this.points[i].get_dischargeDifference();
    		}
    	}
    	return need;
    }
    
    //Method is used to determine the number of offsets available for sale by all of the sellers
    public double calculateSellerSupply() {
    	double supply = 0;
    	for (int i = 0; i < this.numberPoints; i++) {
    		if (this.points[i].get_dischargeDifference() >= 0) {
    			supply += (this.points[i].get_maximumOffsetsForSale() - this.points[i].get_offsetsSold());
    		}
    	}
    	return supply;
    }
    
    
    }
