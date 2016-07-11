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


def generateFeedbackInstance(t, gtDict, numOfusers):
    
    obj = random.choice(gtDict.keys())
    term = "yes" if gtDict.get(obj) == 1 else "no"
    user = "u" + str (random.choice([x for x in range(1,numOfusers+1)]))
    print obj, term, user
    
    return [obj, user, term, t]
     

"""
load the data in a dictionary
"""
def loadDict(fileName, numOfParams):
    
    inputFile = open(fileName, "r")
    
    csvreader = csv.reader(inputFile, delimiter=',', quotechar='"')
    
    dataDict = {}
    
    for record in csvreader:
        paramList = []
        for i in range(0,numOfParams):
            paramList.append(record[i])
        
        # dataDict[(Film,Movie)] = [1 or 0]
        # skip one (hence numOfParams+1 since the 2nd/third entry in the file is score
        # and we are not using it
        dataDict[tuple(paramList)] = int(record[numOfParams+1])
        
    inputFile.close()
        
    return dataDict
    
       

"""
execute the main code
"""
def main():
    
    if len(sys.argv) < 3:
        print "Usage: python SynthesizeFeedback.py [cnds_dir] [out_dir] " + \
              " [reliability_level] [num_of_users] [num_of_fb_instances]"
        sys.exit()


    gtCands = sys.argv[1]
    
    outPath = sys.argv[2]
    
    rl = int(sys.argv[3])
    
    numOfusers = int(sys.argv[4])
    
    numOfinst  = int(sys.argv[5])

    feedbackTypes = {"EntityTypeFB" : ("Cand_EntityType.csv", 1), 
                     "HasTypeFB" : ("Cand_HasType.csv", 2),
                     #"DisjointTypeFB" : ("Cand_DisjointType.csv", 2),
                     "SimEntityTypeFB" : ("Cand_SimEntityType.csv", 2),
                     "HasPropertyFB" : ("Cand_HasProperty.csv", 2) ,
                     "SimPropertyFB" : ("Cand_SimProperty.csv", 2),
                     "SimEntityFB" : ("Cand_SimEntity.csv", 2) }
    
    
    # load data
    for feedbackType in feedbackTypes.keys():
       
        fileName = feedbackTypes.get(feedbackType)[0]
        
        numOfParams = feedbackTypes.get(feedbackType)[1]
        
        dataDict = loadDict(gtCands + "/" + fileName, numOfParams)
        
        feedbackTypes[feedbackType] = (dataDict, fileName, numOfParams)
        
       
    feedbackPool = {}
    
    count = 1
    for idx in range(0, numOfusers * numOfinst):
        
        feedbackType = random.choice(feedbackTypes.keys())
        numOfParams = feedbackTypes.get(feedbackType)[2]
        gtDict = feedbackTypes.get(feedbackType)[0]
        
        # generate a feedback instance consistent with GT
        fbi = generateFeedbackInstance(feedbackType, gtDict, numOfusers)
        
        #print fbi
        flip = [True] * (100-rl) +  [False] * rl 
        
        # flip
        if (random.choice(flip)):
            fbi[2] = "yes" if fbi[2] == "no" else "no"
    
                
        if feedbackPool.has_key(feedbackType):
            # we already have instance for this feedback type
            feedbackPool[feedbackType].append(tuple(fbi))
        else:
            # this is the first instance we see of this feedback type
            feedbackPool[feedbackType] = [tuple(fbi)]
            
        
    # let us write the generated feedback
    for feedbackType in feedbackTypes.keys():
         
         # get the generated instances of this type
        fbInstances = feedbackPool[feedbackType]
         
        outFile = open(outPath + "/" + feedbackType + ".csv", "w")
        csvWriter = csv.writer(outFile, delimiter=',', quotechar='"')
         
        for fbi in fbInstances:
            if len(fbi[0]) == 1:
                csvWriter.writerow([fbi[0][0], fbi[1], fbi[2]])
            else:
                csvWriter.writerow([fbi[0][0], fbi[0][1], fbi[1], fbi[2]])
        
        outFile.close()
#------------------------------------------------------------------------------
if __name__ == "__main__":
    sys.exit(main())