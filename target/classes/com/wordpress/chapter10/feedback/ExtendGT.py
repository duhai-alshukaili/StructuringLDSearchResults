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



"""
extend a list
"""
def  extendSingle(gtList, falseList):
    gtDict = {}
    
    for item in gtList:
        gtDict[item] = 1
        
    limit = len(gtDict)
    count = 0
    
    random.shuffle(falseList)
    
    for item in falseList:
        
        if gtDict.has_key(item):
            continue
        
        gtDict[item] = 0
        count = count + 1
        
        if count == limit:
            return gtDict
    
    return gtDict
#------------------------------------------------------------------------------
"""
Print out the extend ground truth
"""
def emit(extGtData, fileName):
    
    outFile = open(fileName, "w")
    
    csvwriter = csv.writer(outFile, delimiter=',', quotechar='"')
    
    for k in extGtData.keys():
        csvwriter.writerow([k[0], k[1], extGtData.get(k)])
    
    outFile.close()
    
#------------------------------------------------------------------------------

"""
Print out the extend ground truth
"""
def emit2(extGtData, fileName):
    
    outFile = open(fileName, "w")
    
    csvwriter = csv.writer(outFile, delimiter=',', quotechar='"')
    
    for k in extGtData.keys():
        csvwriter.writerow([k, extGtData.get(k)])
    
    outFile.close()
    
#------------------------------------------------------------------------------

"""
Extend the ground truth dictionary with negative atoms
"""
def extend(gtData, param1List, param2List, isSim):
    
    extGtData = copy.deepcopy(gtData)
    
    limit = len(gtData)
    count = 0
    
    random.shuffle(param1List)
    random.shuffle(param2List)
    
    for p1 in param1List:
        for p2 in param2List:
            
            if isSim:
                if p1 == p2:
                    continue
                if gtData.has_key((p2,p1)):
                    continue
            
            if (gtData.has_key((p1,p2))):
                continue
            
            extGtData[(p1,p2)] = 0
            count = count + 1
            
            if count == limit:
                return extGtData
    
    return extGtData      
    
    
#------------------------------------------------------------------------------


"""
Load a file in a list
"""
def loadList(fileName):
    inputFile = open(fileName, "r")
    
    csvreader = csv.reader(inputFile, delimiter=',', quotechar='"')
    
    dataList = []
    
    for record in csvreader:
        #print record[0]
        dataList.append(record[0])
        
        
    inputFile.close()
        
    return dataList 

#------------------------------------------------------------------------------

def loadDict(fileName):
    
    inputFile = open(fileName, "r")
    
    csvreader = csv.reader(inputFile, delimiter=',', quotechar='"')
    
    dataDict = {}
    
    
    
    for record in csvreader:
        dataDict[(record[0], record[1])] = int(record[2])
        
    inputFile.close()
        
    return dataDict
    
       

"""
execute the main code
"""
def main():
    if len(sys.argv) < 2:
        print "Usage: python ExtendGT.py [GT_FOLDER] [OUT_PUT_FOLDER]"
        sys.exit()


    gtPath = sys.argv[1]
    outPath = sys.argv[2]


        
    # load data lists
    entityList = loadList(gtPath + "/GT_Entity.csv")
    entityTypeList = loadList(gtPath + "/GT_EntityType.csv")
    propertyList = loadList(gtPath + "/GT_Property.csv")
    
    
    gtExtConfig = {'GT_HasType.csv': (entityList, entityTypeList),
                   'GT_DisjointType.csv': (entityTypeList, entityTypeList),
                   'GT_SimEntityType.csv': (entityTypeList, entityTypeList),
                   'GT_HasProperty.csv': (entityTypeList, propertyList),
                   'GT_SimProperty.csv': (propertyList, propertyList)}
    
    for fileName in gtExtConfig.keys():
        param1List = gtExtConfig.get(fileName)[0]
        param2List = gtExtConfig.get(fileName)[1]
        
        
        gtData = loadDict(gtPath + "/" + fileName)
        
        extendedGTData = extend(gtData, param1List, param2List, 
                                'sim' in fileName.lower())
        
        emit(extendedGTData, outPath + "/" + fileName)
    
    extEntityDict = extendSingle(entityTypeList , propertyList + entityList)
    
    emit2(extEntityDict, outPath + "/GT_EntityType.csv")
    
#------------------------------------------------------------------------------
if __name__ == "__main__":
    sys.exit(main())