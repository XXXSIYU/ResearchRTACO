package org.fog.utils;

public class Constant {
    public static final double ENERGY_CONSUMPTION_TRANSMISSION = 1.0;
    public static final double ENERGY_CONSUMPTION_PROCESSING = 10.0;
    public static final double ALPHA = 0.3;  // objective function: energy
    public static final double BETA = 0.3;   // objective function: time
    public static final double GAMMA = 0.2;  // objective function: resource utilization
    public static final double DELTA = 0.2;  // objective function: load balance
    
    public static final double ALPHA_TRUST = 2.0;  // Trust-based ACO
    public static final double BETA_TRUST = 1.0;   // Trust-based ACO
    public static final double GAMMA_TRUST = 2.0;  // Trust-based ACO
    
    public static final double APSO_ALPHA = 0.2;  // APSO allocation
    public static final double APSO_BETA = 0.4;   // APSO allocation
    public static final double APSO_GAMMA = 0.4;  // APSO allocation
}
