package com.example.demo4Cat;

import org.knowm.xchart.*;
import org.knowm.xchart.style.PieStyler;
import org.knowm.xchart.style.Styler;
import tech.tablesaw.api.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;



@Path("/wuzzaf")
public class WuzzafDAO{
    Table wuzzaf;

    // 1- Reading the data
    public WuzzafDAO() throws IOException {
        String path = this.getClass().getClassLoader().getResource("").getPath();
        this.wuzzaf = Table.read().csv(path + "/Data/Wuzzuf_Jobs.csv");
    }

    // 2- Display Structure and summary of the data
    @GET
    @Produces("text/plain")
    @Path("/getStructure")
    public String getStructure()
    {
        return wuzzaf.structure().toString();
    }
    @GET
    @Produces("text/plain")
    @Path("/getSummary")
    public String getSummary()
    {
        return wuzzaf.summary().toString();
    }

    // 3- Clean the Data(Null, duplication) return num of removed rows
    @GET
    @Produces("text/plain")
    @Path("/removeDuplicates")
    public String removeDuplicates() {
        int rowsBefore = wuzzaf.rowCount() ;
        wuzzaf = wuzzaf.dropDuplicateRows();
        int rowsAfter = wuzzaf.rowCount();
        return  "Number Of Removed Rows " + (rowsBefore - rowsAfter) ;
    }

    // 4- count the jobs for each company and display that in order
    @GET
    @Produces("text/plain")
    @Path("/getJobsPerCompany")
    public String getJobsPerCompany() {
        return  wuzzaf.countBy("Company").sortDescendingOn("Count").first(10).toString();
    }

    // 6- Find out what are IT the most popular job titles  Top 10 frequent
    @GET
    @Produces("text/plain")
    @Path("/getMostFrequentJobs")
    public String getMostFrequentJobs() {
       Table jobs =  wuzzaf.where(wuzzaf.stringColumn("Skills").containsString("Information Technology (IT)"));
       return jobs.countBy("Title").sortOn(1).last(10).toString();
    }

    // 8- find out the most popular areas
    @GET
    @Produces("text/plain")
    @Path("/getMostPopularAreas")
    public String getMostPopularAreas() {
        return wuzzaf.countBy(wuzzaf.stringColumn("Location")).sortOn(1).last(10).toString();
    }

    // 10-	Print skills one by one and how many each repeated and order the output to find out the most important skills required?
    @GET
    @Produces("text/plain")
    @Path("/getMostImportantSkill")
    public String getMostImportantSkill() {
        List<String> skills = wuzzaf.stringColumn("Skills").asList();
        List<String> allSkills = new ArrayList<>();
        for (String skillSet:skills) {
            for (String skill:skillSet.split(",")) {
                allSkills.add(skill);
            }
        }

        // counting frequency
        Map<String,Integer> temp =new HashMap<>() ;
        for (String s:allSkills.stream().collect(Collectors.toSet())) {
            temp.put(s, Collections.frequency(allSkills,s));
        }

        /////////// find the highest freq skill
        int max = 0 ;
        String nameMax = "";
        for (Map.Entry<String,Integer> entry:temp.entrySet()) {
            if (entry.getValue() > max ){
                max = entry.getValue();
                nameMax = entry.getKey();
            }
        }

        return  nameMax +" Repeated "+ max;
    }

    // 11-	Factorize the YearsExp feature and convert it to numbers in new col. (Bounce )
    @GET
    @Produces("text/plain")
    @Path("/getExpNumCol")
    public String getExpNumCol() {

        List<String> expList = wuzzaf.stringColumn("YearsExp").asList();
        List<String> expSet = expList.stream().collect(Collectors.toSet()).stream().collect(Collectors.toList());
        List<Integer> expNumList = new ArrayList<>();

        for (String expLevel:expList) {
            for (String s:expSet) {
                if(expLevel.equals(s))
                    expNumList.add(expSet.indexOf(s));
            }
        }

        int oldColNum = wuzzaf.columnCount();
        wuzzaf = wuzzaf.addColumns(IntColumn.create("YearsExp In Numbers",expNumList.stream().mapToInt(x -> x+1)));

        return wuzzaf.columnCount() > oldColNum ? "Column Added":"Failled To Add The column" ;
    }


    // Visualization
    // 5- Show step 4 in pie Chart
    public PieChart getPieChart(Map<String,Integer> values) {
        // Create Chart
        PieChart chart = new PieChartBuilder().width(1000).height(600).title("Pie Chart").build();
        // Customize Chart
        chart.getStyler().setCircular(false);
        chart.getStyler().setAnnotationType(PieStyler.AnnotationType.Percentage);
        chart.getStyler().setAnnotationDistance(.82);
        chart.getStyler().setPlotContentSize(.9);

        // Series
        for (Map.Entry<String,Integer> entry:values.entrySet())
            chart.addSeries(entry.getKey(), entry.getValue());
        return chart;
    }
    @GET
    @Produces("text/plain")
    @Path("/getJobsPerCompanyPieChart")
    public String getJobsPerCompanyPieChart() throws IOException {
        Table title  = wuzzaf.countBy("Company").sortDescendingOn("Count").first(10);
        Map<String,Integer> values = new HashMap<>();
        values.put("Others",0);
        for (int i = 0; i < title.rowCount(); i++) {
            values.put(title.getString(i, 0),Integer.parseInt(title.getString(i, 1)));
        }

        PieChart pieChart = getPieChart(values);
        //new SwingWrapper<>(pieChart).displayChart();
        //new SwingWrapper<>(pieChart);
        //Saving in an image

        String path = this.getClass().getClassLoader().getResource("").getPath();
        String pathToChart = path + "/ChartImges/PieChartCompanies.png";
        BitmapEncoder.saveBitmap(pieChart, path +"/ChartImges/PieChartCompanies", BitmapEncoder.BitmapFormat.PNG);
        return  pathToChart;
    }

    // 7- show step 6 in bar chart
    private  CategoryChart getBarChart(Map<String, Integer> values) {
        // Create Chart
        CategoryChart chart = new CategoryChartBuilder().width(1200).height(900).title("Histogram").xAxisTitle("Jobs").yAxisTitle("Number").build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setHasAnnotations(true);
        chart.getStyler().setXAxisLabelRotation(-90);

        // Series
        chart.addSeries("Job Frequencies", values.keySet().stream().collect(Collectors.toList()), values.values().stream().collect(Collectors.toList()));
        return chart;
    }
    @GET
    @Produces("text/plain")
    @Path("/getMostPopITJobsBarChart")
    public String getMostPopITJobsBarChart() throws IOException {
        Map<String,Integer> values = new HashMap<>();
        Table jobs =  wuzzaf.where(wuzzaf.stringColumn("Skills").containsString("Information Technology (IT)"));
        Table temp = jobs.countBy("Title").sortDescendingOn("Count").first(20) ;
        for (int i = 0; i < temp.rowCount(); i++) {
            values.put(temp.getString(i,0),Integer.parseInt(temp.getString(i,1)));
        }

        String path = this.getClass().getClassLoader().getResource("").getPath();
        String PathOfChart = path + "/ChartImges/BarChartPopularITJops.png" ;
        BitmapEncoder.saveBitmap(getBarChart(values), path +"/ChartImges/BarChartPopularITJops", BitmapEncoder.BitmapFormat.PNG);
        return  PathOfChart;
    }

    // 9- show step 8 in bar chart
    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/getMostPopularAreasBarChart")
    public String getMostPopularAreasBarChart() throws IOException {
        Map<String,Integer> values = new HashMap<>();
        Table temp = wuzzaf.countBy(wuzzaf.stringColumn("Location")).sortDescendingOn("Count").first(10) ;
        for (int i = 0; i < temp.rowCount(); i++) {
            values.put(temp.getString(i,0),Integer.parseInt(temp.getString(i,1)));
        }
        String path = this.getClass().getClassLoader().getResource("").getPath();
        String PathOfChart = path + "/ChartImges/BarChartPopularAreas.jpg" ;
        BitmapEncoder.saveBitmap(getBarChart(values),path + "/ChartImges/BarChartPopularAreas", BitmapEncoder.BitmapFormat.JPG);
        return PathOfChart;
    }

}







