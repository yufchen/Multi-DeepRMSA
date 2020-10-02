import numpy as np

TOTAL_SIZE = 10

lambda_req = 12
lambda_intervals = 1/lambda_req

fp = open("./RandExp_TimeTo.txt", "w")
count = 0
while count < TOTAL_SIZE:
    time_to = 0
    while time_to == 0:
        time_to = np.random.exponential(lambda_intervals)
    fp.write(str(time_to) + "\t")
    count += 1
fp.write("\r\n")
fp.close()
