#!/usr/bin/env python

import re, sys



def explode(input_str):
	dest = input_str[0]
	order = int(input_str[1:])
	return (dest, order)

def emit(weightHash, fileName):
	fileHandle = open(fileName, "w")

	keylist = weightHash.keys();
	keylist.sort()

	for key in keylist:
		fileHandle.write(str(weightHash[key]))
		fileHandle.write("\n")

	fileHandle.close()

'''
packed_weight_order = [
	"w1-w17",
	"o1-o12",
	"p1-p12",
	"w18-w21",
	"o13-w15",
	"p13-p14",
	"w22-w23",
	"o16-o20",
	"p15-p15",
	"w24-w29",
	"o21-w23",
	"p16-p16"
]
'''

packed_weight_order = [
	"w1-w7",
	"w14-w17",
	"o1-o15",
	"p1-p4",
	"p7-p8",
	"w20-w21",
	"o16-o18",
	"p13-p13",
	"w22-w23",
	"o19-o21",
	"p15-p15",
	"w24-w29",
	"p16-p16"
]

packed_constant_weight_order = [
	"w8-w13",
	"p5-p6",
	"p9-p12",
	"w18-w19",
	"p14-p14"
]


# explode actual weights
weights_order = []

for i in packed_weight_order:
	pair = i.split("-")

	p1 = explode(pair[0])
	p2 = explode(pair[1])

	for o in range(p1[1], p2[1]+1):
		weights_order.append(p1[0] + str(o))


# explode assigned constants
const_order = []

for i in packed_constant_weight_order:
	pair = i.split("-")

	p1 = explode(pair[0])
	p2 = explode(pair[1])

	for o in range(p1[1], p2[1]+1):
		const_order.append(p1[0] + str(o))



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

w = {}
p = {}
o = {}

for idx in range(len(weights)):
	weight = weights[idx]
	order = weights_order[idx]
	key = int(order[1:])

	if order[0] == 'w':
		# w.append(weight)
		w[key] = weight
	elif order[0] == 'p':
		# p.append(weight)
		p[key] = weight
	elif order[0] == 'o':
		# o.append(weight)
		o[key] = weight

for idx in range(len(const_order)):
	weight = 1
	order = const_order[idx]
	key = int(order[1:])

	if order[0] == 'w':
		# w.append(weight)
		w[key] = weight
	elif order[0] == 'p':
		# p.append(weight)
		p[key] = weight
	elif order[0] == 'o':
		# o.append(weight)
		o[key] = weight


print w
emit(w, "w.txt")
emit(o, "o.txt")
emit(p, "p.txt")