package net.kingingo.server.games.HigherLower;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.kingingo.server.Main;
import net.kingingo.server.utils.TimeSpan;
import net.kingingo.server.utils.Utils;

@AllArgsConstructor
@Getter
@Setter
public class Search {
	
	public String request;
	public String imagePath = "";
	public int amount;
	
	public Search(String[] s) {
		this.request=s[0];
		this.imagePath=s[1];
		this.amount=Integer.valueOf(s[2]);
	}
	
	public Search(DataInputStream in) {
		try {
			this.parseFromInput(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parseFromInput(DataInputStream in) throws IOException {
		this.request=in.readUTF();
		this.imagePath=in.readUTF();
		this.amount = in.readInt();
	}
	
	public void writeToOutput(DataOutputStream out) throws IOException {
		out.writeUTF(request);
		out.writeUTF(imagePath);
		out.writeInt(amount);
	}
	
	public void findImage() {
		if(this.imagePath != null && !this.imagePath.equalsIgnoreCase(""))return;
		String[] urls;
		try {
			urls = findImage(request, 5);
			this.imagePath = urls[Utils.randInt(0, urls.length-1)];
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	public String toString() {
		return "request:"+request+" path:"+imagePath+" amount:"+amount;
	}
	
	private static boolean elementHasClass(WebElement el, String clazz) {
		return el.getAttribute("class").contains(clazz);
	}
	
	public static String[] findImage(String request,int max) throws MalformedURLException {
		String[] urls = new String[max];
		request = request.replaceAll(" ", "+");
    	WebDriver driver = new RemoteWebDriver(new URL("http://localhost:9515"), new ChromeOptions());
	    driver.get("https://www.google.com/search?q="+request+"&tbm=isch");

	    // wait until the google page shows the result
	   /*  WebElement myDynamicElement = (new WebDriverWait(driver, 10)) 
	              .until(ExpectedConditions.presenceOfElementLocated(By. className("rg_i"))); */
    	
	    List<WebElement> findElements = driver.findElements(By.className("rg_i"));
    	Main.printf("findImage", "Found "+findElements.size()+" pictures to "+request);
	    
    	int i = 0;
	    // this are all the links you like to visit
	    for (WebElement webElement : findElements)
	    {
	    	try {
	    		webElement.click();
		    	try {
					Thread.sleep(TimeSpan.SECOND);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			    List<WebElement> els = driver.findElements(By.cssSelector("img"));
			    
			    for(WebElement e : els) {
			    	if(e.getAttribute("alt").equalsIgnoreCase(webElement.getAttribute("alt")) && !elementHasClass(e,"rg_i") && !e.getAttribute("src").contains("data:image")) {
			    		urls[i]=e.getAttribute("src");
			        	Main.printf("findImage", "url: "+urls[i]);
			    		i++;
			    	}
			    }
			    
			    if(i==max)break;
	    	}catch(ElementClickInterceptedException e) {}
	    }
	    driver.close();
	    return urls;
	}
}
