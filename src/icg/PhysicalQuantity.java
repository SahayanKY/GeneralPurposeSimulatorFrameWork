package icg;

import java.util.HashMap;
import java.util.StringJoiner;

import icg.UnitEditor.Prefixs;

/*
 * 物理量及び単位単体、数値単体を取り扱うクラス
 * */
public class PhysicalQuantity {
	public class QuantityElements{
		final Double Number;
		final int prefix, kgDegree, mDegree, sDegree, ADegree;
		QuantityElements(Double Number, int prefix, int kgDegree, int mDegree, int sDegree, int ADegree){
			this.Number = (Number==null)? null : Number*Math.pow(10, prefix);
			this.prefix = prefix;
			this.kgDegree = kgDegree;
			this.mDegree = mDegree;
			this.sDegree = sDegree;
			this.ADegree = ADegree;
		}

		@Override
		public String toString() {
			return toString(Prefixs.k, Prefixs.none, Prefixs.none, Prefixs.none);
		}

		public String toString(Prefixs g_pre, Prefixs m_pre, Prefixs s_pre, Prefixs A_pre) {
			if(!isNonDimension()) {
			StringJoiner joiner = new StringJoiner(" ");

			joiner.add(Double.toString(((Number == null)? 1:Number)*Math.pow(10, -kgDegree*g_pre.n -mDegree*m_pre.n -sDegree*s_pre.n -ADegree*A_pre.n)));
			joiner.add((kgDegree==0)? "":(kgDegree==1)? g_pre.name()+"g":g_pre.name()+"g"+kgDegree);
			joiner.add((mDegree==0)? "":(mDegree==1)? m_pre.name()+"m":m_pre.name()+"m"+mDegree);
			joiner.add((sDegree==0)? "":(sDegree==1)? s_pre.name()+"s":s_pre.name()+"s"+sDegree);
			joiner.add((ADegree==0)? "":(ADegree==1)? A_pre.name()+"A":A_pre.name()+"A"+ADegree);

			return joiner.toString();
			}else {
				return Double.toString(Number);
			}
		}

		public boolean equalsDimension(QuantityElements other) {
			if(kgDegree == other.kgDegree &&
					mDegree == other.mDegree &&
					sDegree == other.sDegree &&
					ADegree == other.ADegree) {
				return true;
			}else {
				return false;
			}
		}

		public boolean isNonDimension() {
			return kgDegree == 0 && mDegree == 0 && sDegree == 0 && ADegree == 0;
		}
	}





	public final QuantityElements elements;

	public PhysicalQuantity(String valueStr) throws IllegalArgumentException{
		HashMap<String,Number> result = UnitEditor.dimensionAnalysis(valueStr);

		Double Number = (result.get("Number") == null)? null : (Double)result.get("Number") * Math.pow(10, (Integer) result.getOrDefault("none", 0));
		Integer kgDegree = (Integer)result.getOrDefault("kg",0),
				mDegree = (Integer)result.getOrDefault("m", 0),
				sDegree = (Integer)result.getOrDefault("s", 0),
				ADegree = (Integer)result.getOrDefault("A", 0);

		//数値も次元もないインスタンスは生成させない
		if(Number == null && kgDegree == 0 && mDegree == 0 && sDegree == 0 && ADegree == 0) {
			throw new IllegalArgumentException();
		}

		this.elements = new QuantityElements(
				Number,	(Integer)result.getOrDefault("none",0),
				kgDegree, mDegree, sDegree, ADegree);
	}

	public boolean isLargerThan(PhysicalQuantity other) throws IllegalArgumentException{
		if(!this.elements.equalsDimension(other.elements) || this.elements.Number == null || other.elements.Number == null) {
			//次元が違うものや、ただの単位同士では比較できない
			throw new IllegalArgumentException("比較対象が不正です");
		}

		if(this.elements.Number > other.elements.Number){
			return true;
		}else{
			return false;
		}
	}


	@Override
	public String toString(){
		return elements.toString();
	}

	public String toString(Prefixs g_pre, Prefixs m_pre, Prefixs s_pre, Prefixs A_pre) {
		return elements.toString(g_pre, m_pre, s_pre, A_pre);
	}
}
