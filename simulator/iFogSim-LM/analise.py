import sys
import collections
import matplotlib.pyplot as plt
import math
import numpy as np
from scipy.stats import sem, t
from scipy import mean
import time

def mean_confidence_interval(data):
  confidence = 0.95
  n = len(data)
  m = mean(data)
  std_err = sem(data)
  h = std_err * t.ppf((1 + confidence) / 2, n - 1)
  return(m)

def error_confidence_interval(data):
  confidence = 0.95
  n = len(data)
  m = mean(data)
  std_err = sem(data)
  h = std_err * t.ppf((1 + confidence) / 2, n - 1)
  return(h)

net_interval1 = []
net_interval2 = []
net_interval3 = []
net_interval4 = []
net_interval5 = []
net_interval6 = []
net_interval7 = []

v_interval1 = []
v_interval2 = []
v_interval3 = []
v_interval4 = []
v_interval5 = []
v_interval6 = []
v_interval7 = []

g_interval1 = []
g_interval2 = []
g_interval3 = []
g_interval4 = []
g_interval5 = []
g_interval6 = []
g_interval7 = []

net_error_interval1 = []
net_error_interval2 = []
net_error_interval3 = []
net_error_interval4 = []
net_error_interval5 = []
net_error_interval6 = []
net_error_interval7 = []

v_error_interval1 = []
v_error_interval2 = []
v_error_interval3 = []
v_error_interval4 = []
v_error_interval5 = []
v_error_interval6 = []
v_error_interval7 = []

g_error_interval1 = []
g_error_interval2 = []
g_error_interval3 = []
g_error_interval4 = []
g_error_interval5 = []
g_error_interval6 = []
g_error_interval7 = []

x=1
while x < 14:
  algolist1 = [] 
  algolist2 = []
  algolist3 = []
  algolist4 = []
  algolist5 = []
  algolist6 = []
  algolist7 = []

  netlist1 = []
  netlist2 = []
  netlist3 = []
  netlist4 = []
  netlist5 = []
  netlist6 = []
  netlist7 = []
 
  vlist1 = []
  vlist2 = []
  vlist3 = []
  vlist4 = []
  vlist5 = []
  vlist6 = []
  vlist7 = []

  glist1 = []
  glist2 = []
  glist3 = []
  glist4 = []
  glist5 = []
  glist6 = []
  glist7 = []

  f = open("guru1.txt","r")
  net = 0 
  v = 0
  g = 0
  for line in f:
    #Let's split the line into an array called "fields" using the ";" as a separator:
    fields = line.split(";")
    #and let's extract the data:
    inputa = fields[0]
    algo = fields[1]
    network = fields[2]
    vsot = fields[3]
    vrgame = fields[4]

    alg = int(algo)
    inp = int(inputa)
    net = float(network)
    v = float(vsot)
    g = float(vrgame)
    
    if (x == inp): 
       #print(alg)   
       if (alg == 1):
          #print(inp,";", algo,";", net,";", v,";", g)
          algolist1.append(algo)
          netlist1.append(net) 
          vlist1.append(v)
          glist1.append(g)

       elif (alg == 2):
          #print(inp,";", algo,";", net,";", v,";", g)
          algolist2.append(algo)
          netlist2.append(net) 
          vlist2.append(v)
          glist2.append(g)

       elif (alg == 3):
          #print(inp,";", algo,";", net,";", v,";", g)
          algolist3.append(algo)
          netlist3.append(net) 
          vlist3.append(v)
          glist3.append(g)

       elif (alg == 4):
          #print(inp,";", algo,";", net,";", v,";", g)
          algolist4.append(algo)
          netlist4.append(net) 
          vlist4.append(v)
          glist4.append(g)
       
       elif (alg == 5):
          #print(inp,";", algo,";", net,";", v,";", g)
          algolist5.append(algo)
          netlist5.append(net) 
          vlist5.append(v)
          glist5.append(g)

       elif (alg == 6):
          #print(inp,";", algo,";", net,";", v,";", g)
          algolist6.append(algo)
          netlist6.append(net) 
          vlist6.append(v)
          glist6.append(g)

       elif (alg == 7):
          #print(inp,";", algo,";", net,";", v,";", g)
          algolist7.append(algo)
          netlist7.append(net) 
          vlist7.append(v)
          glist7.append(g)
  x+=1
  f.close()

  net_interval1.append(mean_confidence_interval(netlist1))
  net_interval2.append(mean_confidence_interval(netlist2))
  net_interval3.append(mean_confidence_interval(netlist3))
  net_interval4.append(mean_confidence_interval(netlist4))
  net_interval5.append(mean_confidence_interval(netlist5))
  net_interval6.append(mean_confidence_interval(netlist6))
  net_interval7.append(mean_confidence_interval(netlist7))

  v_interval1.append(mean_confidence_interval(vlist1))
  v_interval2.append(mean_confidence_interval(vlist2))
  v_interval3.append(mean_confidence_interval(vlist3))
  v_interval4.append(mean_confidence_interval(vlist4))
  v_interval5.append(mean_confidence_interval(vlist5))
  v_interval6.append(mean_confidence_interval(vlist6))
  v_interval7.append(mean_confidence_interval(vlist7))

  g_interval1.append(mean_confidence_interval(glist1))
  g_interval2.append(mean_confidence_interval(glist2))
  g_interval3.append(mean_confidence_interval(glist3))
  g_interval4.append(mean_confidence_interval(glist4))
  g_interval5.append(mean_confidence_interval(glist5))
  g_interval6.append(mean_confidence_interval(glist6))
  g_interval7.append(mean_confidence_interval(glist7))

  net_error_interval1.append(error_confidence_interval(netlist1))
  net_error_interval2.append(error_confidence_interval(netlist2))
  net_error_interval3.append(error_confidence_interval(netlist3))
  net_error_interval4.append(error_confidence_interval(netlist4))
  net_error_interval5.append(error_confidence_interval(netlist5))
  net_error_interval6.append(error_confidence_interval(netlist6))
  net_error_interval7.append(error_confidence_interval(netlist7))

  v_error_interval1.append(error_confidence_interval(vlist1))
  v_error_interval2.append(error_confidence_interval(vlist2))
  v_error_interval3.append(error_confidence_interval(vlist3))
  v_error_interval4.append(error_confidence_interval(vlist4))
  v_error_interval5.append(error_confidence_interval(vlist5))
  v_error_interval6.append(error_confidence_interval(vlist6))
  v_error_interval7.append(error_confidence_interval(vlist7))

  g_error_interval1.append(error_confidence_interval(glist1))
  g_error_interval2.append(error_confidence_interval(glist2))
  g_error_interval3.append(error_confidence_interval(glist3))
  g_error_interval4.append(error_confidence_interval(glist4))
  g_error_interval5.append(error_confidence_interval(glist5))
  g_error_interval6.append(error_confidence_interval(glist6))
  g_error_interval7.append(error_confidence_interval(glist7))




######### plot network
# set width of bar
barWidth = 0.17
 
# set height of bar
bars1 = net_interval1
bars2 = net_interval2
bars3 = net_interval3
bars4 = net_interval4
bars5 = net_interval5
bars6 = net_interval6
bars7 = net_interval7

yer1 = net_error_interval1
yer2 = net_error_interval2
yer3 = net_error_interval3
yer4 = net_error_interval4
yer5 = net_error_interval5
yer6 = net_error_interval6
yer7 = net_error_interval7
 
# Set position of bar on X axis
r1 = np.arange(len(bars1))
r2 = [x + barWidth for x in r1]
r3 = [x + barWidth for x in r2]
r4 = [x + barWidth for x in r3]
r5 = [x + barWidth for x in r4]
r6 = [x + barWidth for x in r5]
r7 = [x + barWidth for x in r6]
 
# Make the plot
plt.bar(r1, bars1, color='#7f6d5f', width=barWidth, edgecolor='white', yerr=yer1, label='Concurrent')
plt.bar(r2, bars2, color='#557f2d', width=barWidth, edgecolor='white', yerr=yer2, label='FCFS-E')
plt.bar(r3, bars3, color='#2d7f5e', width=barWidth, edgecolor='white', yerr=yer3, label='DP-E')
plt.bar(r4, bars4, color='red', width=barWidth, edgecolor='white', yerr=yer4, label='FCFS-I')
plt.bar(r5, bars5, color='yellow', width=barWidth, edgecolor='white', yerr=yer4, label='DP-I')
plt.bar(r6, bars6, color='green', width=barWidth, edgecolor='white', yerr=yer6, label='CB-E')
plt.bar(r7, bars7, color='blue', width=barWidth, edgecolor='white', yerr=yer7, label='CBDP')
 
# Add xticks on the middle of the group bars
plt.xlabel('Inputs', fontweight='bold')
plt.ylabel('Network-Total-Use', fontweight='bold')
plt.xticks([r + barWidth for r in range(len(bars1))], [1,2,3,4,5,6,7,8,9,10,11,12,13])
 
# Create legend & Show graphic
plt.legend()
plt.show()
time.sleep(1)

######### plot VSOT
# set width of bar
barWidth = 0.17
 
# set height of bar
bars1 = v_interval1
bars2 = v_interval2
bars3 = v_interval3
bars4 = v_interval4
bars5 = v_interval5
bars6 = v_interval6
bars7 = v_interval7

yer1 = v_error_interval1
yer2 = v_error_interval2
yer3 = v_error_interval3
yer4 = v_error_interval4
yer5 = v_error_interval5
yer6 = v_error_interval6
yer7 = v_error_interval7
 
# Set position of bar on X axis
r1 = np.arange(len(bars1))
r2 = [x + barWidth for x in r1]
r3 = [x + barWidth for x in r2]
r4 = [x + barWidth for x in r3]
r5 = [x + barWidth for x in r4]
r6 = [x + barWidth for x in r5]
r7 = [x + barWidth for x in r6]
 
# Make the plot
plt.bar(r1, bars1, color='#7f6d5f', width=barWidth, edgecolor='white', yerr=yer1, label='Concurrent')
plt.bar(r2, bars2, color='#557f2d', width=barWidth, edgecolor='white', yerr=yer2, label='FCFS-E')
plt.bar(r3, bars3, color='#2d7f5e', width=barWidth, edgecolor='white', yerr=yer3, label='DP-E')
plt.bar(r4, bars4, color='red', width=barWidth, edgecolor='white', yerr=yer4, label='FCFS-I')
plt.bar(r5, bars5, color='yellow', width=barWidth, edgecolor='white', yerr=yer4, label='DP-I')
plt.bar(r6, bars6, color='green', width=barWidth, edgecolor='white', yerr=yer6, label='CB-E')
plt.bar(r7, bars7, color='blue', width=barWidth, edgecolor='white', yerr=yer7, label='CBDP')
 
# Add xticks on the middle of the group bars
plt.xlabel('Inputs', fontweight='bold')
plt.ylabel('VSOT-Delay', fontweight='bold')
plt.xticks([r + barWidth for r in range(len(bars1))], [1,2,3,4,5,6,7,8,9,10,11,12,13])
 
# Create legend & Show graphic
plt.legend()
plt.show()
time.sleep(1)

######### plot VRGAME
# set width of bar
barWidth = 0.17
 
# set height of bar
bars1 = g_interval1
bars2 = g_interval2
bars3 = g_interval3
bars4 = g_interval4
bars5 = g_interval5
bars6 = g_interval6
bars7 = g_interval7

yer1 = g_error_interval1
yer2 = g_error_interval2
yer3 = g_error_interval3
yer4 = g_error_interval4
yer5 = g_error_interval5
yer6 = g_error_interval6
yer7 = g_error_interval7
 
# Set position of bar on X axis
r1 = np.arange(len(bars1))
r2 = [x + barWidth for x in r1]
r3 = [x + barWidth for x in r2]
r4 = [x + barWidth for x in r3]
r5 = [x + barWidth for x in r4]
r6 = [x + barWidth for x in r5]
r7 = [x + barWidth for x in r6]
 
# Make the plot
plt.bar(r1, bars1, color='#7f6d5f', width=barWidth, edgecolor='white', yerr=yer1, label='Concurrent')
plt.bar(r2, bars2, color='#557f2d', width=barWidth, edgecolor='white', yerr=yer2, label='FCFS-E')
plt.bar(r3, bars3, color='#2d7f5e', width=barWidth, edgecolor='white', yerr=yer3, label='DP-E')
plt.bar(r4, bars4, color='red', width=barWidth, edgecolor='white', yerr=yer4, label='FCFS-I')
plt.bar(r5, bars5, color='yellow', width=barWidth, edgecolor='white', yerr=yer4, label='DP-I')
plt.bar(r6, bars6, color='green', width=barWidth, edgecolor='white', yerr=yer6, label='CB-E')
plt.bar(r7, bars7, color='blue', width=barWidth, edgecolor='white', yerr=yer7, label='CBDP')
 
# Add xticks on the middle of the group bars
plt.xlabel('Inputs', fontweight='bold')
plt.ylabel('VRGAME-DELAY', fontweight='bold')
plt.xticks([r + barWidth for r in range(len(bars1))], [1,2,3,4,5,6,7,8,9,10,11,12,13])
 
# Create legend & Show graphic
plt.legend()
plt.show()





