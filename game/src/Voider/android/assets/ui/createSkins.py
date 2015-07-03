#!/usr/bin/python

import os
import shutil

SKIN_EXT = '.json'
TEMPLATE_SUFFIX = '-template' + SKIN_EXT
BEGIN_VALUE = '${'
BEGIN_LENGTH = len(BEGIN_VALUE)
END_VALUE = '}'
END_LENGTH = len(END_VALUE) - 1

class DensityBucket:
    def __init__(self, suffix, multiplier):
        self.suffix = suffix + SKIN_EXT
        self.multiplier = multiplier

# Fix the values inside the filename
# @param filename name of the file
# @param multiplier what to multiple the values with
def fixValues(filename, multiplier):
    skinFile = open(filename, 'r')
    skinString = skinFile.read()
    skinFile.close()

    while True:
        startIndex = skinString.find(BEGIN_VALUE)
        
        if startIndex != -1:
            endIndex = skinString.find(END_VALUE, startIndex)
            variable = skinString[startIndex:endIndex+1]
            originalValueString = skinString[startIndex+BEGIN_LENGTH:endIndex-END_LENGTH]

            try:
                originalValue = int(originalValueString)
            except ValueError:
                try:
                    originalValue = float(originalValueString)
                except ValueError:
                    print "Unknown value"
                    originalValue = 1

            newValue = originalValue * multiplier
            try:
                newValue = int(newValue)
            except:
                pass

            newValueString = str(newValue)
            print "Replacing " + variable + " -> " + newValueString

            skinString = skinString.replace(variable, newValueString)
        else:
            break

    skinFile = open(filename, 'w')
    skinFile.write(skinString)


SKINS_TO_CREATE = ['general', 'editor', 'game']
DENSITY_BUCKETS = [DensityBucket('-mdpi', 1), DensityBucket('-hdpi', 1.5), DensityBucket('-xhdpi', 2)]

for skin in SKINS_TO_CREATE:
    templateFile = skin + TEMPLATE_SUFFIX
    for density in DENSITY_BUCKETS:
        densityFile = skin + density.suffix
        print "Copying " + templateFile + " -> " + densityFile
        shutil.copy2(templateFile, densityFile)
        fixValues(densityFile, density.multiplier)
