from __future__ import division
import os, re, csv
import hashlib
import random
import operator
import sys
import numpy as np
import pylab as pl
import matplotlib.pyplot as pl
import copy
from sklearn.metrics import auc
from sklearn.metrics import roc_curve
from sklearn.metrics import precision_recall_curve


def generateFeedbackInstance(t, gtDict):
    
    # get a feedback object at random
    obj = random.choice(gtDict.keys())
    
    # assign the term
    term = "yes" if gtDict.get(obj) == 1 else "no"
    
    # assign the user
    user = "u1" 
    
    return [obj, user, term, t]
     

"""
load the data in a dictionary
"""
def loadDict(fileName, numOfParams, reverse):
    
    inputFile = open(fileName, "r")
    
    csvreader = csv.reader(inputFile, delimiter=',', quotechar='"')
    
    dataDict = {}
    
    for record in csvreader:
        paramList = []
        for i in range(0,numOfParams):
            paramList.append(record[i])
        
        if reverse:
            paramList.reverse()
            
        dataDict[tuple(paramList)] = int(record[numOfParams])
        
    inputFile.close()
        
    return dataDict
    
       
def loadExistingFeedback(fileName, numOfParams):
    inputFile = open(fileName, "a+")
    
    # move to the begining of the file
    inputFile.seek(0)
    
    csvreader = csv.reader(inputFile, delimiter=',', quotechar='"')
    
    feedbackPool = []
    
    for record in csvreader:
        paramList = []
        for i in range(0,numOfParams):
            paramList.append(record[i])
        
        user = record[numOfParams]
        term = record[numOfParams+1]
        fbType = record[numOfParams+2]
        
        fbiTuple = (tuple(paramList), user, term, fbType)
        feedbackPool.append(fbiTuple)
    
    return feedbackPool
            

"""
execute the main code
"""
def main():
    
    if len(sys.argv) < 3:
        print "Usage: python Accumulate.py [gt_dir] [out_dir] " + \
              " "
        sys.exit()
    
    gtPath = sys.argv[1]
    outPath = sys.argv[2]
    numOfInst = 20
        
    
    # store feedback instances
    feedbackPool = []
    
    """
    To do: load previous feedback instances
    """
    
    feedback1Pool = loadExistingFeedback(outPath + "/feedback1.csv", 1)
    feedback2Pool = loadExistingFeedback(outPath + "/feedback2.csv", 2)
    
    for fbi in feedback1Pool:
        feedbackPool.append(fbi)
    
    for fbi in feedback2Pool:
        feedbackPool.append(fbi)

    feedbackTypes = {"type_correctness" : ("GT_EntityType.csv", 1, False), 
                     "type_membership" : ("GT_HasType.csv", 2, False),
                     "type_disjointness" : ("GT_DisjointType.csv", 2, False),
                     "type_equivalence" : ("GT_SimEntityType.csv", 2, False),
                     "property_domain" : ("GT_HasProperty.csv", 2, True) ,
                     "property_equivalence" : ("GT_SimProperty.csv", 2, False) }
    
    
    # load data
    for feedbackType in feedbackTypes.keys():
        fileName = feedbackTypes.get(feedbackType)[0]
        numOfParams = feedbackTypes.get(feedbackType)[1]
        reverse = feedbackTypes.get(feedbackType)[2]
        
        dataDict = loadDict(gtPath + "/" + fileName, numOfParams, reverse)
        
        feedbackTypes[feedbackType] = (dataDict, fileName, numOfParams)
        
    
    
    
    count = 0
    while count < numOfInst:
        
        t = random.choice(feedbackTypes.keys())
        numOfParams = feedbackTypes.get(t)[2]
        gtDict = feedbackTypes.get(t)[0]
        
        # generate a feedback instance consistent with GT
        fbi = generateFeedbackInstance(t, gtDict)
        
        flip = [True] * 20 +  [False] * 80
        
        if (random.choice(flip)):
            print "We are filpping"
            fbi[2] = "yes" if fbi[2] == "no" else "no"
            print fbi
        
        fbiTuple = tuple(fbi)
        if fbiTuple not in feedbackPool:
            feedbackPool.append(fbiTuple)
            count = count + 1
        
    
    outFile1 = open(outPath + "/feedback1.csv", "w")
    csvwriter1 = csv.writer(outFile1, delimiter=',', quotechar='"')
    
    outFile2 = open(outPath + "/feedback2.csv", "w")
    csvwriter2 = csv.writer(outFile2, delimiter=',', quotechar='"')
        
    for fbi in feedbackPool:
        
        if len(fbi[0]) == 1:
            csvwriter1.writerow([fbi[0][0], fbi[1], fbi[2], fbi[3]])
        else:
            csvwriter2.writerow([fbi[0][0], fbi[0][1], fbi[1], fbi[2], fbi[3]])
    
    outFile1.close()
    outFile2.close()  
        
#------------------------------------------------------------------------------
if __name__ == "__main__":
    sys.exit(main())