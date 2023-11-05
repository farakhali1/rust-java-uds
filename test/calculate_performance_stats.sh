#!/usr/bin/env bash

REPO_PATH="$(dirname "$(readlink -f "$0")")"
LATENCIES_FILE="output.txt"
TODAY=$(date '+%Y_%m_%d__%H_%M_%S')
DIR_NAME="Performance_stats_$TODAY"
cd $REPO_PATH
mkdir $DIR_NAME

#echo $REPO_PATH/$LATENCIES_FILE

TODAY=$(date '+%Y_%m_%d__%H_%M_%S')
OUTPUT_FILE="results.txt"
echo "--------------------------" >$DIR_NAME/$OUTPUT_FILE
echo "Time: " $TODAY >>$DIR_NAME/$OUTPUT_FILE

COL_NAME="End-To-End"

echo "--------------------------" >>$DIR_NAME/$OUTPUT_FILE
echo "Stats for: " $COL_NAME >>$DIR_NAME/$OUTPUT_FILE
# The first awk counts the number of lines which are numeric. We use a regex here to check if the column is numeric or not.
# ';' stands for Synchronous execution i.e sort only runs after the awk is over.
# The output of both commands are given to awk command which does the whole work.
# So Now the first line going to the second awk is the number of lines in the file which are numeric.
# and from the second to the end line the file is sorted.
(
  awk 'BEGIN {c=0} $1 ~ /^[0-9]/ {c=c+1;} END {print c;}' "$REPO_PATH/$LATENCIES_FILE"
  sort -n "$REPO_PATH/$LATENCIES_FILE" | head -n -5000
) | awk '
  BEGIN {
    c = 0;
    sum = 0;
    sum_of_squares = 0;
    med1_loc = 0;
    med2_loc = 0;
    med1_val = 0;
    med2_val = 0;
    min = 0;
    max = 0;
  }

  #NR is the number of record in a file
  NR==1 {
    LINES = $1

    # We check whether numlines is even or odd so that we keep only
    # the locations in the array where the median might be.
    if (LINES%2==0) {med1_loc = LINES/2-1; med2_loc = med1_loc+1;}
    if (LINES%2!=0) {med1_loc = med2_loc = (LINES-1)/2;}

  }

   $1 ~ /^[0-9]/  &&  NR!=1 {
    # setting min value
    $1=($1/1000)
    if (c==0) {min = $1;}
    all[NR] = $1
   
    # middle two values in array
    if (c==med1_loc) {med1_val = $1;}
    if (c==med2_loc) {med2_val = $1;}
    c++
    sum += $1
    max = $1
  }
  END {
    ave = sum / c
    median = (med1_val + med2_val ) / 2
    sqdif=0
    for (i in all){
        sqdif+=(all[i]-ave)^2
      }

    print "--------------------------"
    print "Mean (us):                " ave
    print "Median (us):              " median
    print "Min (us):                 " min
    print "Max (us):                 " max
    print "Standard Deviation (us):  " (sqdif/c)^0.5
    print "90th Percentile (us):     " all[NR - int(NR*0.1)]
    print "95th Percentile (us):     " all[NR - int(NR*0.05)]
	  print "99th Percentile (us):     " all[NR - int(NR*0.01)]
    print "Total:                    " c
  }
' >>$DIR_NAME/$OUTPUT_FILE
cat $DIR_NAME/$OUTPUT_FILE
rm $REPO_PATH/$LATENCIES_FILE

#done
