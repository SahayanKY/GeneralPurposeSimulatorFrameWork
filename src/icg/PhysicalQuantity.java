package icg;

import java.util.HashMap;
import java.util.StringJoiner;

public class PhysicalQuantity {
	public enum QuantityElements{
		Number,kg,m,s,A;
	}
	
	private HashMap<QuantityElements,Number> map = new HashMap<>();
	
	public PhysicalQuantity(String valueStr){
		HashMap<String,Number> analysisMap = UnitEditor.dimensionAnalysis(valueStr);
		for(QuantityElements e:QuantityElements.values()){
			Number value = analysisMap.get(e.name());
			if(value == null){
				if(e.equals(QuantityElements.Number)){
					throw new IllegalArgumentException();
				}
				value = 0;
			}
			if(e.equals(QuantityElements.Number)){
				value = (Double) value * Math.pow(10,(Integer) analysisMap.getOrDefault("none",0));
			}
			this.map.put(e, value);
		}
	}
	
	public HashMap<QuantityElements,Number> getCharacterizeMap(){
		HashMap<QuantityElements,Number> copyMap = new HashMap<>();
		copyMap.putAll(this.map);
		return copyMap;
	}
	
	public boolean isLargerThan(PhysicalQuantity other){
		boolean result;
	
		for(QuantityElements unit:QuantityElements.values()){
			if(unit.equals(QuantityElements.Number)){
				continue;
			}
			if(!this.map.get(unit).equals(other.map.get(unit))){
				throw new IllegalArgumentException("比較対象が不正");
			}
		}
		
		if((Double)this.map.get(QuantityElements.Number) > (Double) other.map.get(QuantityElements.Number)){
			result = true;
		}else{
			result = false;
		}
		
		
		return result;
	}
	
	@Override
	public String toString(){
		StringJoiner joiner = new StringJoiner(" ");
		for(QuantityElements unit:QuantityElements.values()){
			String sub;
			if(unit.equals(QuantityElements.Number)){
				sub = map.get(unit).toString();
			}else{
				Number degree =  map.get(unit);
				if(degree.equals(0)){
					continue;
				}else if(degree.equals(1)){
					sub = unit.name();
				}else{
					sub = unit+map.get(unit).toString();
				}
			}

			joiner.add(sub);
		}
		return joiner.toString();
	}
}
