import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Date;
import java.util.Random;

/**
 * This file creates a data set that is exactly characterized by 
 * the mixed baseline model.
 * 
 * The mixed baseline model assumes that movies have an inherent
 * quality and raters have an inherent leniency, and the rating
 * for a particular movie is the geometric mean of the quality of
 * the movie and the leniency of the rater.  Other factors are due
 * to noise.
 * 
 *   
 * @author kunal krishna
 *
 */
public class MixedBaselineTestData {

    static Random random = new Random(new Date().getTime());

    /**
     * Main method
     * 
     * @param args command-line specifications
     */
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Error: Usage <Num raters> <Num items> <Noise> <Num ratings> <File>");
            System.exit(0);
        }

        try {
            
            // Get parameters from command line
            int arg = 0;
            int raters = Integer.parseInt(args[arg++]);
            int items = Integer.parseInt(args[arg++]);
            double noise = Double.parseDouble(args[arg++]);
            int ratings = Integer.parseInt(args[arg++]);
            System.out.println("Creating " + ratings + " ratings for file " + args[arg]);
            
            // Set the model parameters
            double item_factors[] = new double[items];
            for (int i = 0; i < items; i++)
                item_factors[i] = 4 * random.nextDouble() + 1;
            
            double rater_factors[] = new double[raters];
            for (int j = 0; j < raters; j++)
                rater_factors[j] = 4 * random.nextDouble() + 1;
            
            // Simulate how program parameters will 
            // be estimated from model parameters
            double item_est_factors[] = new double[items];
            double item_means[] = new double[items];
            double rater_est_factors[] = new double[raters];
            double rater_means[] = new double[raters];
            double total = 0;
            for (int i = 0; i < items; i++)
                for (int j = 0; j < raters; j++) {
                    // Data is geometric mean of factors
                    double v = Math.sqrt(item_factors[i] * rater_factors[j]);
                    
                    // Compute averages
                    item_means[i] += v;
                    rater_means[j] += v;
                    
                    // Double-check baseline estimation code
                    item_est_factors[i] += Math.log(v);
                    rater_est_factors[j] += Math.log(v);
                    total += Math.log(v);
                }
            // Normalize averages
            total /= items * raters;
            for (int i = 0; i < items; i++) {
                item_means[i] /= raters;
                item_est_factors[i] = item_est_factors[i] / raters;
            }
            for (int j = 0; j < raters; j++) {
                rater_means[j] /= items;
                rater_est_factors[j] = rater_est_factors[j] / items;
            }
            
            // Simulate prediction performance on balanced data
            double e1 = 0, e2 = 0, e3 = 0;
            for (int i = 0; i < items; i++)
                for (int j = 0; j < raters; j++) {
                    double v = Math.sqrt(item_factors[i] * rater_factors[j]);
                    double p = Math.exp(item_est_factors[i] + rater_est_factors[j] - total);
                    e1 += (item_means[i] - v) * (item_means[i] - v);
                    e2 += (rater_means[j] - v) * (rater_means[j] - v);
                    e3 += (p - v) * (p - v);
                }
            
            // Normalize and report results
            e1 /= items * raters;
            e2 /= items * raters;
            e3 /= items * raters;
            
            e1 += noise * noise;
            e2 += noise * noise;
            e3 += noise * noise;
            
            System.out.println("Estimated rater baseline RMSE: " + Math.sqrt(e2));
            System.out.println("Estimated item baseline RMSE: " + Math.sqrt(e1));
            System.out.println("Estimated mixed baseline RMSE: " + Math.sqrt(e3));
            
            // Generate actual ratings randomly from the model
            try {
                BufferedWriter w = new BufferedWriter(new FileWriter(args[arg++]));
                for (int i = 0; i < ratings; i++) {
                    int rater = random.nextInt(raters);
                    int item = random.nextInt(items);
                    double rating = Math.sqrt(item_factors[item] * rater_factors[rater]);
                    int round = (int) Math.round(Math.min(5, Math.max(1, rating + noise * random.nextGaussian())));
                    w.write(rater + "\t" + item + "\t" + round + "\n");
                }
                w.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
