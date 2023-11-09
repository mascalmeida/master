import sys
import collections
import matplotlib.pyplot as plt
import math

Result = collections.namedtuple('Result', 'input vsotLoops eegtbLoops networkUsage modulesPerDevice')
EXECUTION_SEPARATOR = "*************************"
INPUT_PATTERN = "input = "
NETWORK_USAGE_PATTERN = "Total network usage = "
VSOT_LOOPS_PATTERN_1 = "[motion_detector, object_detector, object_tracker] ---> "
VSOT_LOOPS_PATTERN_2 = "[object_tracker, PTZ_CONTROL] ---> "
EEGTB_LOOPS_PATTERN = "[EEG_1, client_1, concentration_calculator_1, client_1, DISPLAY_1] ---> "
MODULE_PER_DEVICE_PATTERN = "------------ Module Count By Device ------------"

inputPath = ""

def extractResultsForStrategy(strategy):
    with open(inputPath + "strategy_" + str(strategy), 'r') as file:
        data = file.read()

    executions = splitExecutions(data)
    
    results = []
    for execution in executions:
        if execution != "\n":
            results.append(extractResultFrom(execution))

    return results

def splitExecutions(data):
    return data.split(EXECUTION_SEPARATOR)

def extractTargetStringFromLines(lines, targetPattern):
    targetText = None
    targets = [line for line in lines if targetPattern in line]
    if len(targets) > 0:
        targetText = targets[0].replace(targetPattern,"")

    return targetText

def extractModulePerDeviceCount(execution):
    lines = execution.split(MODULE_PER_DEVICE_PATTERN)[1]
    lines = lines.split('\n')
    lines = [line for line in lines if line]
    
    countPerName = {}

    for line in lines:
        name = line.split(':')[0]
        count = float(line.split(':')[1])
        countPerName[name] = count

    return countPerName

def extractResultFrom(execution):
    lines = execution.split('\n')
    
    # extract input
    inputText = extractTargetStringFromLines(lines, INPUT_PATTERN)
    inputArg = float(inputText.split(" ")[6]) if inputText else 0

    # extract network usage
    target = extractTargetStringFromLines(lines, NETWORK_USAGE_PATTERN)
    network = float(target) if target else 0

    # extract VSOT loops
    vsot = 0
    target = extractTargetStringFromLines(lines, VSOT_LOOPS_PATTERN_1)
    vsot += float(target) if target else 0
    target = extractTargetStringFromLines(lines, VSOT_LOOPS_PATTERN_2)
    vsot += float(target) if target else 0

    # extract EEGTB loops
    target = extractTargetStringFromLines(lines, EEGTB_LOOPS_PATTERN)
    eegtb = float(target) if target else 0

    # extract module per device count
    modulesPerDevice = extractModulePerDeviceCount(execution)

    return Result(input = inputArg, vsotLoops = vsot, eegtbLoops = eegtb, networkUsage = network, modulesPerDevice = modulesPerDevice)

def strategyNameForNumber(number):
    if number == 1:
        return "Concurrent"
    elif number == 2:
        return "FCFS padrão"
    elif number == 3:
        return "Delay-priority padrão"
    elif number == 4:
        return "FCFS individual"
    elif number == 5:
        return "Delay-priority individual"

def strategyMarkerForNumber(number):
    if number == 1:
        return "."
    elif number == 2:
        return "X"
    elif number == 3:
        return "p"
    elif number == 4:
        return "v"
    elif number == 5:
        return "s"

def generateLabelsAndMarkers(strategies):
    strategiesLabels = []
    for strategy in strategies:
        name = strategyNameForNumber(strategy)
        marker = strategyMarkerForNumber(strategy)
        strategiesLabels.append((name, marker))

    return strategiesLabels

def generateGraph(results, inputs, labels, title, xTitle, yTitle, intAxis):
    fig, ax = plt.subplots()
    
    if intAxis:
        maxValue = math.ceil(max(map(lambda x: x[-1], results))) + 1
        
        # Major ticks every 20, minor ticks every 5
        major_ticks = range(0, maxValue, 5)
        minor_ticks = range(0, maxValue, 1)
        
        ax.set_xticks(minor_ticks)
        ax.set_xticks(minor_ticks, minor=True)
        ax.set_yticks(major_ticks)
        ax.set_yticks(minor_ticks, minor=True)
        
        # And a corresponding grid
        ax.grid(which='both', linestyle = ":")
        
        # Or if you want different settings for the grids:
        ax.grid(which='minor', alpha=0.2)
        ax.grid(which='major', alpha=0.8)
    else:
        ax.grid(which='both', linestyle = ":")
    
    
    i = 0
    for result in results:
        (label, marker) = labels[i]
        plt.plot(inputs[i], result, label=label, marker=marker)
        plt.legend(loc='upper left')
        i = i + 1

    plt.xlabel(xTitle)
    plt.ylabel(yTitle)

    plt.title(title)
    plt.savefig(outputPath + title.replace(" ", "_") + ".png")
    plt.show()

def generateModuleCountGraph(inputs, results, strategyName):
    names = set(())
    
    data = []
    namesAndMarkers = []

    for deviceCountDic in results:
        for key in deviceCountDic.keys():
            names.add(key)

    for name in names:
        newData = []
        for deviceCountDic in results:
            newData.append(deviceCountDic.get(name, 0))
        data.append(newData)
        namesAndMarkers.append((name.replace("cloudlet-1", "cloudlet"), "."))

    graphTitle = "Número de módulos por dispositivo - " + strategyName
    dataInputs = []
    for graphLine in data:
        dataInputs.append(inputs)
    generateGraph(data, dataInputs, namesAndMarkers, graphTitle, "Número de usuários movidos", "Número de módulos da aplicação", True)
    
strategies = list(map(lambda x: int(x), sys.argv[1].split(",")))
outputPath = ""
if len(sys.argv) > 2:
    outputPath = sys.argv[2]

inputs = []
labelsAndMarkers = generateLabelsAndMarkers(strategies)
networkResults = []
vsotLoopsResults = []
eegtbLoopsResults = []
modulePerDeviceCountDicResults = []

for strategy in strategies:
    results = extractResultsForStrategy(strategy)
    inputs.append(list(map(lambda result: result.input, results)))
    networkResults.append(list(map(lambda result: result.networkUsage, results)))
    vsotLoopsResults.append(list(map(lambda result: result.vsotLoops, results)))
    eegtbLoopsResults.append(list(map(lambda result: result.eegtbLoops, results)))
    modulePerDeviceCountDicResults.append(list(map(lambda result: result.modulesPerDevice, results)))

generateGraph([[x / 1000 for x in y] for y in networkResults], inputs, labelsAndMarkers, "Uso total da rede", "Número de usuários movidos", "Uso total da rede (KBytes transferidos)", False)
generateGraph(vsotLoopsResults, inputs, labelsAndMarkers, "DCNS", "Número de usuários movidos", "Delay (ms)", False)
generateGraph(eegtbLoopsResults, inputs, labelsAndMarkers, "VRGame", "Número de usuários movidos", "Delay (ms)", False)

index = 0
for strategy in strategies:
    strategyName = strategyNameForNumber(strategy)
    generateModuleCountGraph(inputs[0], modulePerDeviceCountDicResults[index], strategyName)
    index += 1
