from __future__ import division
import os, re, csv, math
import hashlib
import random
import operator
import sys
import numpy as np
import pandas as pd
import matplotlib.pyplot as pl
from sklearn import preprocessing
from sklearn.metrics import auc
from sklearn.metrics import roc_curve
from sklearn.metrics import precision_recall_curve

# generate feedback from the top 5% candidates

def cleanSim(df):
	df1 = pd.DataFrame(columns=df.columns)

	counter = 0

	added = {}
	for row in df.itertuples(index=False):

		key = (row[0],row[1])
		rkey = (row[1],row[0])

		if not added.has_key(key):
			df1.loc[counter] = row[0:]
			added[key] = 1
			added[rkey] = 1
			counter = counter + 1

	return df1
#-------------------------------------------------------------------------------

def fetchCandsFromMid(df, count, hiP, loP, hiMin, loMax):

	feedbackCandList = []

	while count > 0:

		if hiP >= hiMin and count > 0:
			feedbackCandList.append(tuple(df.loc[hiP]))
			count = count - 1
			hiP = hiP - 1

		if loP < loMax and count > 0:
			feedbackCandList.append(tuple(df.loc[loP]))
			count = count - 1
			loP = loP + 1

	return feedbackCandList
#-------------------------------------------------------------------------------

def fetchCandsFromTop(df, count):

	feedbackCandList = []

	idx = 0

	while count > 0:

		feedbackCandList.append(tuple(df.loc[idx]))
		count = count - 1
		idx = idx + 1

	return feedbackCandList
#-------------------------------------------------------------------------------


def generateFeedback(fbCandsPath, outFilePath):

	print "Processing ", fbCandsPath
	
	headers = {"SimEntity_result.csv": ["E1" ,"E2", "Prob" ,"GT"],
	       "SimProperty_result.csv": ["P1","P2","Prob", "GT"],
	       "SimEntityType_result.csv": ["T1","T2","Prob","GT"],
	       "EntityType_result.csv": ["T","Prob","GT"],
	       "HasType_result.csv": ["E","T","Prob","GT"],
	       "HasProperty_result.csv": ["T","P","Prob","GT"]}

	inFileName = os.path.basename(fbCandsPath)

	df = pd.read_csv(fbCandsPath, names=headers[inFileName])

	# clean up similarity files
	if inFileName.startswith("Sim"):
		df = cleanSim(df)

	# scale the prob to find the mid points
	scaler = preprocessing.MinMaxScaler()
	prob = df.Prob.copy()
	prob_scaled = scaler.fit_transform(prob)

	# add the scaled prob. to the data frame
	df["Prob_scaled"] = prob_scaled

	# candidate are the things with prob > 0
	df = df[df.Prob_scaled > 0]

	# number of candidate
	candCount = df.shape[0]

	print "Total number of candidates = %d" % (candCount) 

	# amount of feedback
	feedbackAmount = int(math.ceil(0.05 * candCount))

	print "Total amount of feedback to be generated = %d" % (feedbackAmount) 

	# get a copy of the new column prob_scaled
	probScaled = df["Prob_scaled"].copy()

	# find the median
	median = probScaled.median()

	# get the indecies of the data in the middle
	midIdx = probScaled[probScaled == median]

	# two pointers that indicate the boundry of
	# the feedback region
	loP = 0
	hiP = 0

	# list of feedback candidates
	feedbackCandList = []


	feedbackCandList = fetchCandsFromTop(df, feedbackAmount)

	'''
	# find the intital boundries of the feedback region
	# depending on wether we find the median in the 
	# scaled prob. array or not.
	if len(midIdx) > 0:
		print "Median %f found in probScaled" % (median)

		# get the data farme of all the points in the middle
		# midDf = df[probScaled == median]

		
		middlePoint = midIdx.index[0]
		
		# assign lowP and hiP
		loP = middlePoint + 1
		hiP = middlePoint -1

		feedbackCandList = fetchCandsFromMid(df, feedbackAmount - 1, hiP, loP, 0, len(df))
		feedbackCandList.append(tuple(df.loc[middlePoint]))
	else:
		print "Median %f not found in probScaled" % (median)

		for idx in range(0,len(df)):
			row = tuple(df.loc[idx])
			if row[-1] < median:
				loP = idx
				hiP = idx - 1
				break

		feedbackCandList = fetchCandsFromMid(df, feedbackAmount, hiP, loP, 0, len(df))
	'''

	# now we generate feedback 
	# number of data paramters
	params = 2
	if inFileName.startswith("EntityType"):
		params = 1

	feedbackList = []
	dfColumns = []
	for fbCand in feedbackCandList:
		fbi = ()

		term = "yes" if fbCand[-2] == 1 else "no"

		if params == 1:
			fbi = tuple([fbCand[0], "U1", term])
			dfColumns = ["A", "UID", "Term"]
		else:
			dfColumns = ["A", "B", "UID", "Term"]
			fbi = tuple([fbCand[0], fbCand[1], "U1", term])

		feedbackList.append(fbi)


	# create a dataframe for the feedback
	feedbackDF = pd.DataFrame(columns=dfColumns)

	for i in range(0,len(feedbackList)):
		feedbackDF.loc[i] = list(feedbackList[i])

	print "Writing to ", outFilePath
	feedbackDF.to_csv(outFilePath, index=False, header=False)

#-------------------------------------------------------------------------------


files = [("SimEntity_result.csv", "SimEntityFB.csv"),
	     ("SimProperty_result.csv", "SimPropertyFB.csv") ,
	     ("SimEntityType_result.csv", "SimEntityTypeFB.csv") ,
	     ("EntityType_result.csv", "EntityTypeFB.csv") ,
	     ("HasType_result.csv", "HasTypeFB.csv") ,
	     ("HasProperty_result.csv", "HasPropertyFB.csv")]

inputPath = sys.argv[1]
outputPath = sys.argv[2]


for pair in files:
	generateFeedback(inputPath + "/" + pair[0], outputPath + "/" + pair[1])
