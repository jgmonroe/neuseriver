package sim.app.neuseriver;

public class pointSource {
	/*Index value */
	private int index;
	/*Willingness to accept */
	private double wta;
	/*First trade willingness to pay  */
	private double wtp1;
	/*Second trade willingness to pay  */
	private double wtp2;
	/*Transport factor */
	private double transportFactor;
	/*Initial concentration of nitrogen (mg/L) */
	private double initialConc;
	/*Initial discharge of point source (lbs of N / year) */
	private double initialDischarge;
	/*Current discharge of point source (lbs of N / year) */
	private double currentDischarge;
	/*Initial discharge permit limit (lbs of N / year) */
	private double initialLimit;
	/*Current discharge limit (lbs of N / year) */
	private double currentLimit;
	/*Difference between the discharge limit and the current discharge (lbs of N) */
	private double dischargeDifference;
	/*Offsets bought from other sources (lbs of N) */
	private double offsetsBought;
	/*Offsets sold to other sources (lbs of N) */
	private double offsetsSold;
	/*Total amount of nitrogen being treated after initial year (lbs of N) */
	private double treatmentAmount;
	/*Total amount of nitrogen being treated after initial year (mg N / L) */
	private double treatmentAmountConc;
	/*Growth in discharge of nitrogen beyond the initial discharge limit (lbs of N) */
	private double growth;
	/*Treatment budget ($) */
	private double budget;
	/*Flow rate (MGD) */
	private double flowRate;
	/*Current concentration of nitrogen in discharge stream (mg/L) */
	private double currentConc;
	/*Minimum concentration of nitrogen that can be in the discharge stream based on treatment limitations (mg/L) */
	private double minimumConc;
	/*Max offsets from treatment available */
	private double maxTreatOffsets;
	/*Total Nitrogen Offsets available for sell */
	private double totalOffsetsAvailable;
	/*X^2 coefficient a in formula y = aX^2 + bX + C */
	private double costX2;
	/*X coefficient b in formula y = aX^2 + bX + C  */
	private double costX1;
	/*Constant value C in formula y = aX^2 + bX + C  */
	private double costConstant;
	/*Minimum total nitrogen reduction (mg/L) for cost function */
	private double rangeMin;
	/*Maximum total nitrogen reduction (mg/L) for cost function */
	private double rangeMax;
	/*Initial lbs N per year that can be sold because current treatment level is below NPDES permit limit */
	private double initialOffsetsAvailable;
	/*Max Nitrogen Offsets from treatment available for sell at initialization (lbs N / year) */
	private double originalMaxTreatOffsets;
	/*Maximum offsets available for purchase at beginning of simulation */
	private double maximumOffsetsForSale;
	/*Monthly Nitrogen Discharge (lbs N / month) */
	private double monthlyDischarge;
	/*Monthly Nitrogen Discharge at Estuary (lbs Nest / month) */
	private double monthlyDischargeEst;

	public pointSource(int index, double transportFactor, double flowRate, double initialConc, double dischargeLimit, double costX2, double costX1, double costConstant, double rangeMin, double rangeMax) {
		this.index = index;
		this.wta = 0.0;
		this.wtp1 = 0.0;
		this.wtp2 = 0.0;
		this.transportFactor = transportFactor;
		this.flowRate = flowRate;
		this.initialConc = initialConc;
		this.currentConc = initialConc;
		calc_initialDischarge();
		this.currentDischarge = this.initialDischarge;
		this.initialLimit = dischargeLimit;
		this.dischargeDifference = (dischargeLimit - this.initialDischarge);
		calc_initialOffsetsAvailable();
		this.currentLimit = dischargeLimit;
		this.costX2 = costX2;
		this.costX1 = costX1;
		this.costConstant = costConstant;
		this.rangeMin = rangeMin;
		this.rangeMax = rangeMax;
		set_minimumConc();
		originalMaxTreatOffsetsCalc();
		maxTreatmentOffsetsCalc();
		calc_maximumOffsetsForSale();
		update_monthlyDischarge();
		update_monthlyDischargeEst();
		// TODO Auto-generated constructor stub
	}
	
	//Point Source index value getter
	//Current state of program doesn't support mutation of index value
	public int get_Index() {
		return this.index;
	}
	
	//Willingness to accept observer
	public double get_wta() {
		return this.wta;
	}
		
	//Willingness to accept setter
	public void set_wta(double wta_new) {
		this.wta = wta_new;
	}
	
	//Willingness to pay observer (First Trade)
	public double get_wtp1() {
		return this.wtp1;
	}
			
	//Willingness to pay setter (First Trade)
	public void set_wtp1(double wtp1_new) {
		this.wtp1 = wtp1_new;
	}
	
	//Willingness to pay 2 observer (Second Trade)
	public double get_wtp2() {
		return this.wtp2;
	}
				
	//Willingness to pay 2 setter (Second Trade)
	public void set_wtp2(double wtp2_new) {
		this.wtp2 = wtp2_new;
	}
	
	//Transport factor observer
	public double get_transportFactor() {
		return this.transportFactor;
	}
			
	//Transport factor setter
	public void set_transportFactor(double transportFactor_new) {
		this.transportFactor = transportFactor_new;
	}
	
	//Initial discharge observer
	public double get_initialDischarge() {
		return this.initialDischarge;
	}
				
	//Calculates the initial discharge from the point source in units of lbs of N per year
	//This method should be called at initialization
	public void calc_initialDischarge() {
		this.initialDischarge = this.initialConc * (1.0/1000.0) * (1.0/453.592) * 3.78541 * (this.flowRate*1000000.0) * 365.0;
	}
	
	//Update treatment amount
	public void update_treatmentAmount() {
		if (this.offsetsSold <= this.initialOffsetsAvailable) {
			this.treatmentAmount = 0;
		} else {
			this.treatmentAmount = this.offsetsSold - this.initialOffsetsAvailable;
		}
	}
	
	//Current discharge observer
	public double get_currentDischarge() {
		return this.currentDischarge;
	}
					
	//Update current discharge
	public void update_currentDischarge() {
		//this.currentDischarge = (this.initialDischarge - this.treatmentAmount + this.growth);
		//double averageConcentration = 3.0; //mg/L
		//double averageFlow = 4.5; //MGD
		this.currentDischarge = (this.initialConc - this.treatmentAmountConc) * (1.0/1000.0) * (1.0/453.592) * 3.78541 * (this.flowRate*1000000.0) * 365.0; //Convert concentration and average yearly flow into lbs N/year
	}
	
	//Initial discharge permit limit observer
	public double get_initialLimit() {
		return this.initialLimit;
	}
				
	//Initial discharge permit limit setter
	public void set_initialLimit(double initialLimit_new) {
		this.initialLimit = initialLimit_new;
	}
	
	//Current discharge permit limit observer
	public double get_currentLimit() {
		return this.currentLimit;
	}
					
	//Current discharge permit limit updater
	public void update_currentLimit() {
		this.currentLimit = (this.initialLimit + this.offsetsBought - this.offsetsSold);
	}
	
	//Update the discharge difference value
	public void update_dischargeDifference() {
		this.dischargeDifference = (this.currentLimit - this.currentDischarge);
	}
	
	//Observer for current discharge difference
	public double get_dischargeDifference() {
		return this.dischargeDifference;
	}
	
	//Updates the growth value for the point source
	public void update_growth(double new_growth) {
		this.growth = new_growth;
	}
	
	//Adds to the number of offsets bought
	public void add_offsetsBought(double pounds) {
		this.offsetsBought += pounds;
	}
	
	//Adds to the number of offsets bought
	public void add_offsetsSold(double pounds) {
		this.offsetsSold += pounds;
	}
	
	//Getter function for total offsets sold
	public double get_offsetsSold() {
		return this.offsetsSold;
	}
		
	//Getter function for total offsets bought
	public double get_offsetsBought() {
		return this.offsetsBought;
	}
	
	//Determines if point source needs to buy pollution offsets
	public boolean isBuyer() {
		if (this.dischargeDifference < 0) {
			return true;
		} else {
			return false;
		}
	}
	
	//Determines if point source has pollution offsets to sell
	public boolean isSeller() {
		if (this.dischargeDifference >= 0) {
			if (this.maxTreatOffsets <= 0 && this.offsetsSold >= this.initialOffsetsAvailable) {
				return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	//Calculates maximum offsets available from treating
	public void maxTreatmentOffsetsCalc() {
		double delta;
		if (this.currentConc <= this.minimumConc) {
			delta = 0;
			this.maxTreatOffsets = 0;
		} else {
			delta = this.currentConc - this.minimumConc;
			double offsets = this.flowRate * 1000000.0 * 365.0 * 3.78541 * (1.0/1000.0) * (1.0/453.592) * delta;
			this.maxTreatOffsets = offsets;
		}
	}
	
	//Calculates original maximum offsets available from treating
	public void originalMaxTreatOffsetsCalc() {
		double delta;
		if (this.currentConc <= this.minimumConc) {
			delta = 0;
			this.originalMaxTreatOffsets = 0;
		} else {
			delta = this.currentConc - this.minimumConc;
			double offsets = this.flowRate * 1000000.0 * 365.0 * 3.78541 * (1.0/1000.0) * (1.0/453.592) * delta;
			this.originalMaxTreatOffsets = offsets;
		}
	}
	
	//Calculates total offsets available for sale
	public void totalOffsetsCalc() {
		this.totalOffsetsAvailable = this.maxTreatOffsets + this.dischargeDifference;
	}
	
	//Calculates new discharge concentration based on offsets sold from additional treatment and updates treatment amount
	//offsets are in units of (lbs N / year)
	public void updateCurrentConc(){
		this.currentConc = this.initialConc - (this.treatmentAmount * 453.592 * 1000.0 * (1.0/(this.flowRate * 1000000.0)) * (1.0/365.0) * (1.0/3.78541));
	}
	
	public void updateTreatmentConc() {
		this.treatmentAmountConc = (this.treatmentAmount * 453.592 * 1000.0 * (1.0/(this.flowRate * 1000000.0)) * (1.0/365.0) * (1.0/3.78541));
	}
	
	//This method calculates the current marginal cost of treating 1 lb of Nest per year 
	public double calcCurrentWTP() {
		if (this.currentConc <= this.minimumConc) {
			//System.out.println("Current Concentration is below Minimum Concentration!!!");
			return 0;
		} else {
			//System.out.println(((2 * this.costX2 * this.treatmentAmount) + this.costX1) * (1.0/365.0) * 1000.0 * 453.592);
			return ((1/this.transportFactor) * ((2 * this.costX2 * this.treatmentAmountConc) + this.costX1) * (1.0/(this.flowRate*1000000.0)) * (1.0/365.0) * (1.0/3.78541) * 1000.0 * 453.592);
		}
	}
	
	//This method calculates the minimum concentration possible for the point source. This should be called at initialization.
	public void set_minimumConc() {
		this.minimumConc = this.initialConc - this.rangeMax;
	}
	
	//This methods calculates the initial offsets that are available for sale without treatment
	//This method should be called at initialization
	public void calc_initialOffsetsAvailable() {
		this.initialOffsetsAvailable = this.dischargeDifference;
	}
	
	//This method calculates the maximum offsets available for purchase at beginning of simulation 
	//This method should be called at initialization
	public void calc_maximumOffsetsForSale() {
		this.maximumOffsetsForSale = this.initialOffsetsAvailable + this.originalMaxTreatOffsets;
	}
	
	//Getter function for maximum offsets available for purchase at beginning of simulation
	public double get_maximumOffsetsForSale() {
		return this.maximumOffsetsForSale;
	}
	
	//Updates the monthly discharge of Nitrogen into the Neuse
	public void update_monthlyDischarge() {
		this.monthlyDischarge = this.currentDischarge / 12.0;
	}
	
	//Updates the monthly discharge of Nitrogen into the Neuse Estuary
	public void update_monthlyDischargeEst() {
		this.monthlyDischargeEst = this.monthlyDischarge * this.transportFactor;
	}
	
	//Updates the monthly discharge of Nitrogen into the Neuse
	public double get_monthlyDischarge() {
		return this.monthlyDischarge;
	}
		
	//Updates the monthly discharge of Nitrogen into the Neuse Estuary
	public double get_monthlyDischargeEst() {
		return this.monthlyDischargeEst;
	}
	
}
