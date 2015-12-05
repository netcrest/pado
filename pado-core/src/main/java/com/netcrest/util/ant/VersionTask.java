/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.util.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

/*
 * Disclaimer
 * ----------
 * This class is a part of Netcrest's NAF (Netcrest Application Framework).
 * Any modifications to this class is not allowed unless approved by Netcrest
 * in a written form.
 */

/**
 * VersionTask generates a version class with the version information
 * provided by the user. The version string has the following format.
 * <p>
 * <code>
 * &lt;major&gt;.&lt;minor&gt;.&lt;update&gt;-B&lt;build&gt;
 * <br>where
 * <br>&lt;major&gt; is the major version number,
 * <br>&lt;minor&gt; is the minor version number,
 * <br>&lt;update&gt; is the update version number,
 * <br>&lt;build&gt; is the build number</code>
 * <br><pre>For example, 1.0.1-B1, 1.0.2-B2, etc.</pre>
 * <p>
 * Upon successful execution, this task sets the following properties where
 * the default &lt;prefix&gt is "build".
 * <ul>
 * <li><prefix>.version - The version number.</li>
 * <li><prefix>.version.date - the version date.</li>
 * <li><prefix>.distribution - The distribution name.</li>
 * <li><prefix>.created-by - The user name.</li>
 * <li><prefix>.main.class - The main class name invokable from jar.</li>
 * <li><prefix>.repository.tag - The repository tag. This property is set only if version.created is set.</li>
 * <li><prefix>.created - true if the new version is created.</li>
 * </ul>
 * @author dpark
 * @version 1.1
 */
public class VersionTask extends Task
{
    private boolean mReadOnly = false;
    private String mPrefix = "build.";

    private String mProjectName;
    private int mMajorVersion = -1;
    private int mMinorVersion = -1;
    private int mUpdateNumber = -1;
    private String mBuildNumber = null;
    private String mBuildDate;
    private String mClassName = "VersionClass";
    private String mSourceDir;
    private String mBuilderName;

    /**
     * Executes this task. This is the execution entry point.
     * @throws BuildException Thrown if it encounters errors due to insufficient
     *                        information.
     */
    public void execute() throws BuildException
    {
        try {
            if (mReadOnly) {
            	try {
            		VersionData prevVersion = new VersionData(mClassName);
            		
            		// Set the version properties.
                    setProperty("version", prevVersion.getVersion());
                    setProperty("version.date", prevVersion.getVersionDate());
                    setProperty("distribution", prevVersion.getDistributionName());
                    setProperty("created-by", prevVersion.getBuilderName());
                    setProperty("main.class", mClassName);
                    setProperty("repository.tag", prevVersion.getRepositoryTag());
                    setProperty("created", "false");  
            		
            	} catch (ClassNotFoundException ex) {
     
            	}

            	return;
            }

            mBuildDate = getTodaysDate();
            String classPath = mClassName.replace('.', '/');
            String fileName = mSourceDir + "/" + classPath + ".java";
            File file = new File(fileName);
            VersionData prevVersion = null;
            try {
                prevVersion = new VersionData(mClassName);
            } catch (ClassNotFoundException ex) {
                String val;
                System.out.println("The version class, " + mClassName + " not found.");
                do {
                    System.out.println("Do you want to create one? (y, n)");
                    val = readLine();
                    val = val.trim().toLowerCase();
                } while (!val.equals("y") && !val.equals("n"));
                if (val.equals("n")) {
                    System.out.println("The version class, " + mClassName + " does not exist.");
                    System.out.println("This build will not be versioned.");
                    System.out.println("Continuing without " + mClassName + "...");
                    return;
                } else {
                    setProperty("version.previous.exists", "true");
                }
            }

            readVersion(prevVersion);

            if (file.exists()) {
                if (file.delete() == false) {
                    throw new BuildException("Unable to access the file " + fileName + ". Please check the file access previledge.");
                }
            }
            int index = mClassName.lastIndexOf('.');
            String packageName = mClassName.substring(0, index);
            String shortName = getShortClassName(mClassName);
            FileWriter writer = new FileWriter(fileName);
            writer.write("package " + packageName + ";\n\n");
            writer.write("/**\n");
            writer.write(" * Auto-generated by Ant using " + getClass().getName() + ".\n");
            writer.write(" * Please do not edit.\n");
            writer.write(" * <p>Copyright: Copyright (c) 2013\n");
            writer.write(" * <p>Company: Netcrest Technologies, LLC\n");
            writer.write(" * <p>Date: " + mBuildDate + "\n");
            writer.write(" */\n");
            writer.write("public class " + shortName + "\n");
            writer.write("{" + "\n");
            writer.write("    public final static int major = " +  mMajorVersion + ";\n");
            writer.write("    public final static int minor = " + mMinorVersion + ";\n");
            writer.write("    public final static int update = " + mUpdateNumber + ";\n");
            writer.write("    public final static String projectName = " + "\"" + mProjectName + "\";\n");
            writer.write("    public final static String build = " + "\"" + mBuildNumber + "\";\n");
            writer.write("    public final static String buildDate = \"" + mBuildDate + "\";\n");
            writer.write("    public final static String builderName = \"" + mBuilderName + "\";\n");
            writer.write("    public final static String VERSION = \"" + getVersion() + "\";\n");
            writer.write("    public final static String REPOSITORY_TAG = \"" + getRepositoryTag() + "\";\n");
            writer.write("\n");
            writer.write("    public final static String getVersion()\n");
            writer.write("    {\n");
            writer.write("        return VERSION;\n");
            writer.write("    }\n\n");
            writer.write("    public final static String getProjectName()\n");
            writer.write("    {\n");
            writer.write("        return projectName;\n");
            writer.write("    }\n\n");
            writer.write("    public final static String getBuilderName()\n");
            writer.write("    {\n");
            writer.write("        return builderName;\n");
            writer.write("    }\n\n");
            writer.write("    public final static String getDistributionName()\n");
            writer.write("    {\n");
            writer.write("        return projectName + \"_\" + getVersion();\n");
            writer.write("    }\n\n");
            writer.write("    public final static String getVersionDate()\n");
            writer.write("    {\n");
            writer.write("        return VERSION + \" \" + buildDate;\n");
            writer.write("    }\n\n");
            writer.write("    public final static String getRepositoryTag()\n");
            writer.write("    {\n");
            writer.write("        return REPOSITORY_TAG;\n");
            writer.write("    }\n\n");
            writer.write("    public final static void main(String args[])\n");
            writer.write("    {\n");
            if (mProjectName == null) {
                writer.write("        System.out.println(\"       Project: <undefined>\");\n");
            } else {
                writer.write("        System.out.println(\"       Project: \" + getProjectName());\n");
            }
            writer.write("        System.out.println(\"       Version: \" + getVersionDate());\n");
            writer.write("        System.out.println(\"Repository Tag: \" + getRepositoryTag());\n");
            writer.write("        System.out.println(\"      Built by: \" + getBuilderName());\n");
            writer.write("    }\n");
            writer.write("}" + "\n");
            writer.close();
            
            // Set the version properties.
            setProperty("version", getVersion());
            setProperty("version.date", getVersionDate());
            setProperty("distribution", getDistributionName());
            setProperty("created-by", mBuilderName);
            setProperty("main.class", mClassName);
            setProperty("repository.tag", getRepositoryTag());
            setProperty("created", "true");

        } catch (IOException ex) {
            throw new BuildException(ex);
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }

    /**
     * Sets the read only flag. If this flag is set to true then it only reads the
     * current version and returns without entering the version setup process.
     * It ignores all other attribute settings except for "prefix". The version
     * information can be retrieved from "version.class.version" and
     * "version.repository.tag". If it is set to false then it enters the
     * version setup process. The default value is false.
     * @param pReadOnly true to read the current class version, false to enter
     *                  the version setup process.
     */
    public void setReadOnly(boolean pReadOnly)
    {
        mReadOnly = pReadOnly;
    }

    /**
     * Set a prefix for the properties. If the prefix does not end with a "."
     * one is automatically added. If the prefix is null or an empty string,
     * then the default prefix value of "build" is assigned.
     */
    public void setPrefix(String pPrefix)
    {
        this.mPrefix = pPrefix;
        if (mPrefix == null) {
            mPrefix = "build.";
        }
        mPrefix = mPrefix.trim();
        if (!mPrefix.endsWith(".") == false) {
            this.mPrefix += ".";
        }
    }

    /**
     * Returns String formatted version including date.
     */
    private String getVersionDate()
    {
        return getVersion() + " " + mBuildDate;
    }
    
    /**
     * Returns String formatted version.
     */
    private String getVersion()
    {
        if (mBuildNumber != null) {
            return  mMajorVersion + "." + mMinorVersion + "." + mUpdateNumber + "-B" + mBuildNumber;
        } else {
            return  mMajorVersion + "." + mMinorVersion + "." + mUpdateNumber;
        }
    }
    
    /**
     * Returns the distribution name typically used for naming
     * the project distribution.
     */
    private String getDistributionName()
    {
    	return mProjectName + "_" + getVersion();
    }
    
    /**
     * Returns the name of the version class.
     */
    private String getClassName()
    {
    	return mClassName;
    }

    /**
     * Returns the repository tag created based on the version. The format is
     * "&lt;project_name&gt;_RELEASE_&lt;major&gt;_&lt;minor&gt;_&lt;update&gt;-B&lt;build&gt;".
     * For example, "naf_1.0.0-B1".
     */
    private String getRepositoryTag()
    {
        String tag = "";
        if (mProjectName != null) {
            tag = mProjectName;
        }
        if (mBuildNumber != null) {
            tag += "_" + mMajorVersion + "." + mMinorVersion + "." + mUpdateNumber + "-B" + mBuildNumber;
        } else {
            tag += "_" + mMajorVersion + "." + mMinorVersion + "." + mUpdateNumber;
        }
        return tag;
    }

    /**
     * Returns String formatted today's date.
     */
    private String getTodaysDate()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
        return format.format(calendar.getTime());
    }

    /**
     * Sets the project name.
     * @param pProjectName The name of the project being built. This is an optional
     *                     property used to name the repository tag.
     */
    public void setProject(String pProjectName)
    {
        mProjectName = pProjectName;
    }

    /**
     * Sets the major version number.
     * @param pMajorVersion The major version number.
     */
    public void setMajor(int pMajorVersion)
    {
        mMajorVersion = pMajorVersion;
    }

    /**
     * Sets the minor version number.
     * @param pMinorVersion The minor version number.
     */
    public void setMinor(int pMinorVersion)
    {
        mMinorVersion = pMinorVersion;
    }

    /**
     * Sets the update number.
     * @param pUpdateNumber The update number.
     */
    public void setUpdateNumber(int pUpdateNumber)
    {
        mUpdateNumber = pUpdateNumber;
    }

    /**
     * Sets the build number.
     * @param pBuildNumber The build number. This number is appended to the
     *                     version with the prefix "-B". VerstionTask interprets
     *                     the build number of the format "x.x", i.e., 1 or 1.3.
     *                     If another format is used then it constructs the
     *                     default value given at the input with "1" appended
     *                     at the end.
     */
    public void setBuildNumber(String pBuildNumber)
    {
        mBuildNumber = pBuildNumber;
    }

    /**
     * Sets the source directory of the version class. The version class is
     * created in the specified directory.
     * @param pSourceDir The source directory.
     */
    public void setSrcdir(Path pDir)
    {
        mSourceDir = pDir.toString();
    }

    /**
     * Sets the fully qualified class name of the version class.
     * @param pClassName The fully qualified class name of the version class.
     */
    public void setClass(String pClassName)
    {
        mClassName = pClassName;
    }

    /**
     * Sets the builder name.
     * @param pBuilderName The builder name.
     */
    public void setBuilderName(String pBuilderName)
    {
        mBuilderName = pBuilderName;
    }

    /**
     * Returns user inputs
     * @throws IOException Thrown if it encounters an I/O error.
     */
    private String readLine() throws IOException
    {
        byte data[] = new byte[256];
        byte b;
        int offset = 0;
        while ((b = (byte)System.in.read()) != 0xa) {
            data[offset++] = b;
        }
        return new String(data, 0, offset);
    }

    /**
     * Reads version info from the user inputs.
     * @param pPrevVersion The previous version. Null allowed.
     * @throws IOException Thrown if it encounters an I/O error.
     */
    private void readVersion(VersionData pPrevVersion) throws IOException
    {
        int defaultMajor;
        int defaultMinor;
        int defaultUpdate;
        String defaultBuild;

        if (pPrevVersion == null) {
            pPrevVersion = new VersionData(mClassName, mBuildDate, 1, 0, 0, "");
        }
        String prevVersionName = null;
        try {
            prevVersionName = pPrevVersion.getVersion();
        } catch (Exception ex) {
            // ignore
        }
        String prevRepositoryTag = null;
        try {
            prevRepositoryTag = pPrevVersion.getRepositoryTag();
        } catch (Exception ex) {
            // ignore
        }
        if (prevVersionName == null) {
            System.out.println("           Previous Version: <undefined>");
        } else {
            System.out.println("           Previous Version: " + prevVersionName);
        }
        if (prevRepositoryTag == null) {
            System.out.println("    Previous Repository Tag: <undefined>");
        } else {
            System.out.println("    Previous Repository Tag: " + prevRepositoryTag);
        }

        // Set major version
        System.out.println("\nPlease enter build version information. If you wish to use");
        System.out.println("the default values shown in [], just hit the 'Enter' key.");
        System.out.print("Enter Major Version [" + pPrevVersion.major + "]: ");
        System.out.flush();
        String val = readLine();
        val = val.trim();
        if (val.length() == 0) {
            mMajorVersion = pPrevVersion.major;
        } else {
            mMajorVersion = Integer.parseInt(val);
        }

        // Set minor version
        if (mMajorVersion != pPrevVersion.major) {
            defaultMinor = 0;
        } else {
            defaultMinor = pPrevVersion.minor;
        }
        System.out.print("Enter Minor Version [" + defaultMinor + "]: ");
        System.out.flush();
        val = readLine();
        val = val.trim();
        if (val.length() == 0) {
            mMinorVersion = defaultMinor;
        } else {
            mMinorVersion = Integer.parseInt(val);
        }

        // Set update number
        if (mMinorVersion != pPrevVersion.minor) {
            defaultUpdate = 0;
        } else {
            defaultUpdate = pPrevVersion.update;
        }
        System.out.print("Enter Update Number [" + defaultUpdate + "]: ");
        System.out.flush();
        val = readLine();
        val = val.trim();
        if (val.length() == 0) {
            mUpdateNumber = defaultUpdate;
        } else {
            mUpdateNumber = Integer.parseInt(val);
        }

        // Set build number
        if (mUpdateNumber != pPrevVersion.update) {
            defaultBuild = "1";
        } else {
            if (pPrevVersion.build == null) {
                defaultBuild = "1";
            } else {
                try {
                    int intVal = 0;
                    int remainder = 0;
                    int index = pPrevVersion.build.indexOf(".");
                    if (index == -1) {
                        intVal = Integer.parseInt(pPrevVersion.build);
                        defaultBuild = "" + (intVal + 1);
                    } else {
                        intVal = Integer.parseInt(pPrevVersion.build.substring(0, index));
                        remainder = Integer.parseInt(pPrevVersion.build.substring(index + 1));
                        defaultBuild = "" + intVal + "." + (remainder + 1);
                    }
                } catch (Exception ex) {
                    defaultBuild = pPrevVersion.build + "1";
                }
            }
        }
        System.out.print("Enter Build Number [" + defaultBuild + "]: ");
        System.out.flush();
        val = readLine();
        val = val.trim();
        if (val.length() == 0) {
            mBuildNumber = defaultBuild;
        } else {
            mBuildNumber = val;;
        }

        // Set builder name
        System.out.print("Enter Builder Name [" + pPrevVersion.builderName + "]: ");
        System.out.flush();
        val = readLine();
        val = val.trim();
        if (val.length() == 0) {
            mBuilderName = pPrevVersion.builderName;
        } else {
            mBuilderName = val;
        }

        // Continue or re-enter
        while (true) {
            if (prevVersionName == null) {
                System.out.println("           Previous Version: <undefined>");
            } else {
                System.out.println("           Previous Version: " + prevVersionName);
            }
            if (prevRepositoryTag == null) {
                System.out.println("    Previous Repository Tag: <undefined>");
            } else {
                System.out.println("    Previous Repository Tag: " + prevRepositoryTag);
            }
            System.out.println("        New Version Entered: " + getVersion());
            System.out.println("         New Repository Tag: " + getRepositoryTag());
            System.out.println();
            System.out.println("Enter 'c' to continue, 'r' to re-enter.");
            val = readLine();
            val = val.trim();
            if (val.equalsIgnoreCase("c") || val.equalsIgnoreCase("r")) {
                break;
            }
        }

        // If 'r' then re-enter.
        if (val.equalsIgnoreCase("r") ) {
            readVersion(pPrevVersion);
        }
    }

    /**
     * Sets the property.
     */
    private void setProperty(String pName, String pValue) {
        getProject().setNewProperty(mPrefix + pName, pValue);
    }
    
    private String getShortClassName(String className)
    {
        if (className == null) {
            return null;
        }
        String shortName = null;
        int index = className.lastIndexOf('.');
        if (index == -1 || index >= className.length()-1) {
            shortName = className;
        } else {
            shortName = className.substring(index+1);
        }
        return shortName;
    }

    /**
     * VersionData contains version information.
     */
    public static class VersionData
    {
    	String className;
        int major;
        int minor;
        int update;
        String build;
        String buildDate;
        String builderName;

        /**
         * Constructs a VersionData object from the version class provided.
         * @throws ClassNotFoundException Thrown if the version class is not found.
         * @throws NoSuchFieldException Thrown if an expected field is not found.
         * @throws IllegalAccessException Thrown if a field is not accessible.
         */
        public VersionData(String className) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException
        {
        	this.className = className;
            Class clas = Class.forName(this.className);
            Field field = clas.getField("major");
            major = field.getInt(clas);
            field = clas.getField("minor");
            minor = field.getInt(clas);
            field = clas.getField("update");
            update = field.getInt(clas);

            // If the build field is not found then look for the patch field.
            // This is for backward compatibility. The patch field has been
            // deprecated.
            try {
                field = clas.getField("build");
            } catch (Exception ex) {
                field = clas.getField("patch");
            }

            build = (String)field.get(clas);
            field = clas.getField("buildDate");
            buildDate = (String)field.get(clas);
            field = clas.getField("builderName");
            builderName = (String)field.get(clas);
        }

        /**
         * Constructs a VersionData object based on the given version information.
         * @param pMajor The major version number.
         * @param pMinor The minor version number.
         * @param pUpdate The update version number.
         * @param pBuild The build number string.
         */
        VersionData(String className, String buildDate, int pMajor, int pMinor, int pUpdate, String pBuild)
        {
        	this.className = className;
        	this.buildDate = buildDate;
            major = pMajor;
            minor = pMinor;
            update = pUpdate;
            build = pBuild;
            builderName = "Ant";
        }

        /**
         * Returns the String formatted version (release).
         * @throws IllegalAccessException
         * @throws ClassNotFoundException Thrown if the version class is not found.
         * @throws NoSuchFieldException Thrown if an expected field is not found.
         * @throws InvocationTargetException Thrown if it is unable to invoke getVersion() method.
         * @throws IllegalAccessException Thrown if a field is not accessible.
         */
        String getVersion()
                throws ClassNotFoundException,
                        NoSuchMethodException,
                        InvocationTargetException,
                        IllegalAccessException
        {
            Class clas = Class.forName(className);
            Method method = clas.getMethod("getVersion", null);
            return (String)method.invoke(clas, null);
        }
        
        String getVersionDate()
		        throws ClassNotFoundException,
		                NoSuchMethodException,
		                InvocationTargetException,
		                IllegalAccessException
		{
		    Class clas = Class.forName(className);
		    Method method = clas.getMethod("getVersionDate", null);
		    return (String)method.invoke(clas, null);
		}
        
        String getDistributionName()
		        throws ClassNotFoundException,
		                NoSuchMethodException,
		                InvocationTargetException,
		                IllegalAccessException
		{
		    Class clas = Class.forName(className);
		    Method method = clas.getMethod("getDistributionName", null);
		    return (String)method.invoke(clas, null);
		}
        
        String getBuilderName()
		        throws ClassNotFoundException,
		                NoSuchMethodException,
		                InvocationTargetException,
		                IllegalAccessException
		{
		    Class clas = Class.forName(className);
		    Method method = clas.getMethod("getBuilderName", null);
		    return (String)method.invoke(clas, null);
		}
        
        String getClassName()
		        throws ClassNotFoundException,
		                NoSuchMethodException,
		                InvocationTargetException,
		                IllegalAccessException
		{
		    Class clas = Class.forName(className);
		    Method method = clas.getMethod("getClassName", null);
		    return (String)method.invoke(clas, null);
		}


        /**
         * Returns the String formatted repository tag.
         * @throws IllegalAccessException
         * @throws ClassNotFoundException Thrown if the version class is not found.
         * @throws NoSuchFieldException Thrown if an expected field is not found.
         * @throws InvocationTargetException Thrown if it is unable to invoke getVersion() method.
         * @throws IllegalAccessException Thrown if a field is not accessible.
         */
        String getRepositoryTag()
                throws ClassNotFoundException,
                        NoSuchMethodException,
                        InvocationTargetException,
                        IllegalAccessException
        {
            Class clas = Class.forName(className);
            Method method = clas.getMethod("getRepositoryTag", null);
            return (String)method.invoke(clas, null);
        }
    }
}
