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
    
    inputFile.close()
    
    return feedbackPool
            

"""
execute the main code
"""
def main():
    
    
    gtDir = "/home/ispace/Documents/programming/eclipse/" + \
    "LDPayGo2/data/gt_data/movies2/"
    
    candDir = "/home/ispace/Documents/programming/eclipse/" + \
    "LDPayGo2/data/feedback/movies2/cands/"
    
    entityList = []
    
    # load GT_Entity.csv
    inputFile = open(gtDir + "GT_Entity.csv" , "r")
    
    csvreader = csv.reader(inputFile, delimiter=',', quotechar='"')
    
    entityList = []
    for record in csvreader:
         entityList.append(record[0])
    
    inputFile.close()
    
    entityList = list(set(entityList))
    

        
    #################################################################
    
    # load GT_SimEntity.csv
    inputFile = open(gtDir + "GT_SimEntity.csv" , "r")
    
    csvreader = csv.reader(inputFile, delimiter=',', quotechar='"')
    
    simEntityList = []
    for record in csvreader:
        param = (record[0], record[1])
        simEntityList.append(param)
    
    inputFile.close()
    
  
    
    
    candList = []
    
    for e1 in entityList:
        for e2 in entityList:
            
            if (e1 != e2):
                params = (e1, e2)
                
                if params in simEntityList:
                    candList.append((e1,e2,-1,1))
                else:
                    candList.append((e1,e2,-1,0))
    
    
    outFile1 = open(candDir + "Cand_SimEntity.csv", "w")
    csvwriter1 = csv.writer(outFile1, delimiter=',', quotechar='"')
    
    
    for e in candList:
        csvwriter1.writerow([e[0],e[1],e[2], e[3]])
    
    outFile1.close()

        
    
    
    
   
        
#------------------------------------------------------------------------------
if __name__ == "__main__":
    sys.exit(main())