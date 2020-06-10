/**
java日期转js日期
**/
function javaTojsTime (strTime) {
    var date = new Date(strTime);
    return date = new Date(date.valueOf() - 60* 60 * 1000*14);// 当前时间减掉14小时
    //return date.getFullYear()+"-"+(date.getMonth()+1)+"-"+date.getDate()+" "+date.getHours()+":"+date.getMinutes()+":"+getSeconds();
}