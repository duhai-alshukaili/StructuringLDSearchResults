
from __future__ import division
import os, re, csv
import hashlib
import random
import operator
import sys
import numpy as np
#import pylab as pl
import matplotlib.pyplot as pl
from sklearn.metrics import auc
from sklearn.metrics import roc_curve
from sklearn.metrics import precision_recall_curve


if len(sys.argv) < 3:
    print "Usage: python auc.py result.csv iterationName"
    sys.exit()

resultsFile = sys.argv[1]
iteration = sys.argv[2]
#fbs = int(sys.argv[3])


results = open(resultsFile, "r")

csvreader = csv.reader(results, delimiter=',', quotechar='"')


# ground truth and probablity
gTrue = []
scores = []

for record in csvreader:
    recSize = len(record)
    gTrue.append(int(record[recSize-1]))    # last element
    scores.append(float(record[recSize-2])) # 2nd to last element




results.close()

# calculate area under PR curve
y_true = np.array(gTrue)
y_scores = np.array(scores)
precision, recall, thresholds = precision_recall_curve(y_true, y_scores)
#print "Recall: ", recall
#print "Precesion: ", precision
#print "Thresholds: ", thresholds


area = auc(recall, precision)
print("%s: %0.5f" % (iteration.strip(),area))

# plot the curve
pl.clf()
pl.plot(recall, precision, 'ro')
pl.plot(recall, precision, '--' ,label='AUC PR (' + iteration.strip() + ')')
pl.ylabel('Precision')
pl.xlabel('Recall')
pl.ylim([0.0, 1.05])
pl.xlim([0.0, 1.0])
pl.title('AUC PR=%0.5f (%s)' % (area, iteration.strip()))
#pl.legend(loc="upper right")
#pl.show()

figure = pl.gcf() # get current figure
#figure.set_size_inches(8, 6)
# when saving, specify the DPI
#pl.savefig("pr.png", dpi = 100)
# pl.savefig(iteration.strip() + ".png", dpi = 100)

