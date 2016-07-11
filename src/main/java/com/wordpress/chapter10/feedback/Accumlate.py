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


def generateFeedbackInstance(cndDict):
    
    # get a feedback object at random
    obj = random.choice(cndDict.keys())
    
    # assign the term
    term = "yes" if cndDict.get(obj) == 1 else "no"
    
    # assign the user
    user = "u1" 
    
    return [obj, user, term]
     



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
        
        # ex: dataDict[(Film,Movie)] = [1 or 0]
        # skip one (hence numOfParams+1 since the 2nd/third entry in the file is score
        # and we are not using it
        dataDict[tuple(paramList)] = int(record[numOfParams+1])
        
    inputFile.close()
        
    return dataDict
       
"""
Load existing feedback in a dictionary
"""
def loadExistingFeedback(fileName, numOfParams):
    inputFile = open(fileName, "a+")
    
    # move to the begining of the file
    inputFile.seek(0)
    
    csvreader = csv.reader(inputFile, delimiter=',', quotechar='"')
    
    feedbackPool = {}
    
    for record in csvreader:
        paramList = []
        for i in range(0,numOfParams):
            paramList.append(record[i])
        
        user = record[numOfParams]
        term = record[numOfParams+1]
        
        fbiTuple = [tuple(paramList), user, term]
        feedbackPool[tuple(paramList)] = fbiTuple
    
    return feedbackPool
            

"""
execute the main code
"""
def main():
    
    if len(sys.argv) < 2:
        print "Usage: python Accumulate.py [cnds_dir] [out_dir] "
        sys.exit()
    
    cndsPath = sys.argv[1]
    outPath = sys.argv[2]
    
    
    feedbackTypes = {"EntityTypeFB" : ("Cand_EntityType.csv", 1), 
                     "HasTypeFB" : ("Cand_HasType.csv", 2),
                     #"DisjointTypeFB" : ("Cand_DisjointType.csv", 2),
                     "SimEntityTypeFB" : ("Cand_SimEntityType.csv", 2),
                     "HasPropertyFB" : ("Cand_HasProperty.csv", 2) ,
                     "SimPropertyFB" : ("Cand_SimProperty.csv", 2),
                     "SimEntityFB" : ("Cand_SimEntity.csv", 2) }
        
    

    

            
    for feedbackType in feedbackTypes.keys():
        
       
        candFileName = feedbackTypes.get(feedbackType)[0]
        
        numOfParams = feedbackTypes.get(feedbackType)[1]
        
        candDict = loadDict(cndsPath + "/" + candFileName, numOfParams)
        
        numOfCands = len(candDict)
        
        numOfInstPerRound = int(numOfCands * 0.05)
        
        
        #feedbackTypes[feedbackType] = (dataDict, fileName, numOfParams)
        
        # load instances of existing feedback
        exsitingFileName = outPath + "/" + feedbackType + ".csv";
        feedbackPool = loadExistingFeedback(exsitingFileName, numOfParams)
        
        count = 0
        while count < numOfInstPerRound:
            fbi = generateFeedbackInstance(candDict)
            obj = fbi[0]
            
            if obj not in feedbackPool.keys():
                count = count + 1
                feedbackPool[obj] = fbi
        
        outFile = open(outPath + "/" + feedbackType + ".csv", "w")
        csvWriter = csv.writer(outFile, delimiter=',', quotechar='"')
        
        for obj in feedbackPool.keys():
            fbi = feedbackPool[obj]
            
            if len(fbi[0]) == 1:
                csvWriter.writerow([fbi[0][0], fbi[1], fbi[2]])
            else:
                csvWriter.writerow([fbi[0][0], fbi[0][1], fbi[1], fbi[2]])
        
        outFile.close()
            
#------------------------------------------------------------------------------
if __name__ == "__main__":
    sys.exit(main())