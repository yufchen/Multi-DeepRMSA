import re
import matplotlib.pyplot as plt
from pylab import *

def get_BlockingProb_in_file(filename):
    '''
    Get the number in each line of the file containing "Blocking Probability"
    return a list
    :param filename:
    :return:
    '''
    result = []
    with open(filename, "r") as fp:
        line = fp.readline()
        while line:
            iff = re.findall(r"Blocking Probability", line)
            if iff:
                result.append(float(re.findall(r"\d+\.?\d*", line)[0]))
            line = fp.readline()
    return result


def get_agenttime_in_file(filename):
    '''
    Get the number in each line of the file containing "Blocking Probability"
    return a list
    :param filename:
    :return:
    '''
    result = []
    with open(filename, "r") as fp:
        line = fp.readline()
        while line:
            iff = re.findall(r"Agent Process time", line)
            if iff:
                result.append(float(re.findall(r"\d+\.?\d*", line)[0]))
            line = fp.readline()
    return result

if __name__ == "__main__":
    # CTRL1_time_list = get_agenttime_in_file("./CTRL1_Episode_Time.log")
    # print("CTRL1 processing time(ms)(per episode): ", mean(CTRL1_time_list))
    # print("CTRL1 processing time(ms)(per req): ", mean(CTRL1_time_list)/1000)
    #
    # CTRL2_time_list = get_agenttime_in_file("./CTRL2_Episode_Time.log")
    # print("CTRL2 processing time(ms)(per episode): ", mean(CTRL2_time_list))
    # print("CTRL2 processing time(ms)(per req): ", mean(CTRL2_time_list) / 1000)
    #
    # Broker_time_list = get_agenttime_in_file("./Broker_Episode_Time.log")
    # print("Broker processing time(ms)(per episode): ", mean(Broker_time_list))
    # print("Broker processing time(ms)(per req): ", mean(Broker_time_list) / 1000)

    #standalone case
    # CTRL_Blocking_episode_list = get_BlockingProb_in_file("./result2/Benchmark_Episode_Time_action_idx.log")
    # CTRL_episode_num = []
    # for i in range(len(CTRL_Blocking_episode_list)):
    #     CTRL_episode_num.append(i + 1)
    #
    # CTRL_Blocking_episode_list_baseline = get_BlockingProb_in_file("./result2/Benchmark_Episode_Time_action_id0.log")
    # CTRL_episode_num_baseline = []
    # for i in range(len(CTRL_Blocking_episode_list_baseline)):
    #     CTRL_episode_num_baseline.append(i + 1)
    #
    # plt.plot(CTRL_episode_num, CTRL_Blocking_episode_list, color='green', label='CTRLer')
    # plt.plot(CTRL_episode_num_baseline, CTRL_Blocking_episode_list_baseline, color='blue', label='CTRLer_base')
    # xmax = max(len(CTRL_episode_num), len(CTRL_episode_num_baseline))
    #
    # plt.grid(ls=":", c='b', )
    # minorticks_on()
    # plt.legend()
    # plt.xlabel("Training Episode")
    # plt.ylabel("Blocking Probability")
    #
    # plt.axis([0, xmax, 0, 0.3])
    # plt.savefig("./training_result/standlone.png")
    # plt.show()





    # Draw the result blocking probability
    CTRL1_Blocking_episode_list = get_BlockingProb_in_file("./CTRL1_Episode_Time2.log")
    CTRL1_episode_num = []
    for i in range(len(CTRL1_Blocking_episode_list)):
        CTRL1_episode_num.append(i + 1)

    CTRL2_Blocking_episode_list = get_BlockingProb_in_file("./CTRL2_Episode_Time2.log")
    CTRL2_episode_num = []
    for i in range(len(CTRL2_Blocking_episode_list)):
        CTRL2_episode_num.append(i + 1)

    Broker_Blocking_episode_list = get_BlockingProb_in_file("./Broker_Episode_Time2.log")
    Broker_episode_num = []
    for i in range(len(Broker_Blocking_episode_list)):
        Broker_episode_num.append(i+1)


    #start draw graph
    #plt.title("")
    plt.plot(CTRL1_episode_num, CTRL1_Blocking_episode_list, color='green', label='CTRLer1')
    plt.plot(CTRL2_episode_num, CTRL2_Blocking_episode_list, color='blue', label='CTRLer2')
    plt.plot(Broker_episode_num, Broker_Blocking_episode_list, color='red', label='Broker')
    xmax = max(len(CTRL1_episode_num), len(CTRL2_episode_num), len(Broker_episode_num))


    plt.grid(ls=":",c='b',)
    minorticks_on()
    #plt.axhline(y=0.12, ls="--", c="black")
    #plt.axhline(y=0.17, ls="--", c="black")
    #plt.hlines(0.12, 0, xmax, colors="black", linestyles="dashed")
    #plt.hlines(0.15, 0, xmax, colors="black", linestyles="dashed")

    plt.legend()
    plt.xlabel("Training Episode")
    plt.ylabel("Blocking Probability")

    plt.axis([0, xmax, 0, 0.3])
    plt.savefig("./training_result/Blocking3.png")
    plt.show()