import pandas as pd
from scipy import stats
import numpy as np

# Single machine data
single_machine_data = {
    "thread_count": [1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024],
    "avg_time_us": [
        253566778, 169267908.4, 106353540.4, 73290812.6, 69459317.2,
        67020685.4, 66046572.4, 64586798.8, 64821837.6, 64703686.8, 64957338.6
    ]
}

# Distributed (two machines) data
distributed_data = {
    "thread_count": [1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024],
    "avg_time_us": [
        96476227.6, 96563818.2, 55705400.2, 45087711.2, 45322512.2,
        44029527, 44894443.4, 44797162.8, 43867472.8, 43805533.6, 44260029.8
    ]
}

# Convert to DataFrame
single_machine_df = pd.DataFrame(single_machine_data)
distributed_df = pd.DataFrame(distributed_data)

# Calculate differences in runtime for each thread count
differences = single_machine_df["avg_time_us"] - distributed_df["avg_time_us"]
differences_df = pd.DataFrame({
    "thread_count": single_machine_df["thread_count"],
    "difference_us": differences
})

# Perform the paired t-test
t_stat, p_value = stats.ttest_rel(
    single_machine_df["avg_time_us"], 
    distributed_df["avg_time_us"])

# Prepare the results
t_test_results = {
    "t_statistic": t_stat,
    "p_value": p_value,
    "mean_difference_us": np.mean(differences),
    "std_deviation_difference_us": np.std(differences, ddof=1) 
}

for key, value in t_test_results.items():
    print(f"{key}: {value}")

# Conclusion
if p_value < 0.05:
    print("Reject the null hypothesis.")
else:
    print("Accept the null hypothesis.")
