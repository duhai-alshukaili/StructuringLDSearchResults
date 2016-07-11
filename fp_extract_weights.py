#!/usr/bin/env python

import re, sys



def explode(input_str):
	dest = input_str[0]
	order = int(input_str[1:])
	return (dest, order)

def emit(weightList, fileName):
	fileHandle = open(fileName, "w")

	for weight in weightList:
		fileHandle.write(str(weight))
		fileHandle.write("\n")

	fileHandle.close()


packed_weight_order = [
	"f1-f18",
	"p1-p8"
]



weights_order = []

for i in packed_weight_order:
	pair = i.split("-")

	p1 = explode(pair[0])
	p2 = explode(pair[1])

	for o in range(p1[1], p2[1]+1):
		weights_order.append(p1[0] + str(o))


#pattern = r'{(.+)}.'


pattern = r'{([+-]?(\d+(\.\d*)?|\.\d+)([eE][+-]?\d+)?)}.+'

fileInput = open(sys.argv[1])

weights = []

for line in fileInput:

    matchObj = re.match(pattern, line)

    if matchObj:
        weights.append(float(matchObj.group(1)))
        #print line


fileInput.close()


if len(weights) != len(weights_order):
	print "Weights-Order mismatch. Weights=%d, Order=%d" % (len(weights), len(weights_order))
	sys.exit(1)

p = []
f = []

for idx in range(len(weights)):
	weight = weights[idx]
	order = weights_order[idx]

	if order[0] == 'w':
		w.append(weight)
	elif order[0] == 'p':
		p.append(weight)
	elif order[0] == 'o':
		o.append(weight)
	elif order[0] == 'f':
		f.append(weight)



emit(f, "f.txt")