package icg;

import java.util.HashMap;
import java.util.StringJoiner;

import icg.UnitEditor.Prefixs;

/*
 * 物理量及び単位単体、数値単体を表現するクラス。
 * 主に物理量または単位の変換、大小の比較を行う。数値計算に直接用いるのは推奨されない。
 * */
public class PhysicalQuantity {

	/*
	 * 数値、単位の次数を保持、文字列表現への変換を担うクラス
	 * */
	public class QuantityElements{
		final Double Number;
		final int prefix, kgDegree, mDegree, sDegree, ADegree;

		/*コンストラクタ*/
		QuantityElements(Double Number, int prefix, int kgDegree, int mDegree, int sDegree, int ADegree){
			this.Number = (Number==null)? null : Number*Math.pow(10, prefix);
			this.prefix = prefix;
			this.kgDegree = kgDegree;
			this.mDegree = mDegree;
			this.sDegree = sDegree;
			this.ADegree = ADegree;
		}


		/*
		 * kg m s Aを用いた物理量の文字列表現を返す
		 * @return MKSA単位系での物理量の文字列表現
		 * */
		@Override
		public String toString() {
			return toString(Prefixs.k, Prefixs.none, Prefixs.none, Prefixs.none);
		}

		/*
		 * g,m,s,Aの各々に用いる接頭辞を指定し、その文字列表現を返す。
		 * 例えば、g_preに「Prefixs.M」と指定した場合"Mg"を用いて物理量を表現する。
		 * @param
		 * String g_pre:gに用いる接頭辞(他同様)
		 * @return 指定の接頭辞を用いた物理量の文字列表現
		 * */
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

		/*
		 * この物理量を構成する要素群の内、kg,m,s,Aの数値、即ち次元が、
		 * 指定の他の要素群と一致するかを判定する。
		 * @param 比較対象の要素群
		 * @return 次元が一致する場合true
		 * */
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

		/*
		 * この要素群によって特徴付けられる量が無次元量なのかを判定する
		 * @return 無次元量である場合false
		 * */
		public boolean isNonDimension() {
			return kgDegree == 0 && mDegree == 0 && sDegree == 0 && ADegree == 0;
		}
	}



	public final QuantityElements elements;

	/*
	 * コンストラクタ。
	 * 指定された文字列により物理量(または数値、単位のみ)を表すインスタンスを生成する。
	 * */
	public PhysicalQuantity(String valueStr) throws IllegalArgumentException,NullPointerException{
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


	/*
	 * 物理量(または数値)同士の大小関係を比較する。指定された2つのインスタンス間の比較が不正な
	 * ものであればIllegalArgumentExceptionがスローされる。
	 * @param 比較対象の物理量(または数値)
	 * @return このインスタンス物理量の方が指定の物理量よりも大きければtrue
	 * @throws 2つの物理量の次元が異なる場合、2つの内どちらかが単位だけの場合、
	 * 比較できない不正であり、IllegalArgumentExceptionがスロー。
	 * */
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


	/*
	 * このインスタンスのMKSA単位系に則った文字列表現を返す。
	 * @return MKSA単位系によるインスタンスの文字列表現
	 * */
	@Override
	public String toString(){
		return elements.toString();
	}

	/*
	 * 指定の接頭辞を用いてこのインスタンスの文字列表現を行い、返す。
	 * @param g_pre gに用いる接頭辞(他同様)
	 * @return 指定の接頭辞を用いた文字列表現
	 * */
	public String toString(Prefixs g_pre, Prefixs m_pre, Prefixs s_pre, Prefixs A_pre) {
		return elements.toString(g_pre, m_pre, s_pre, A_pre);
	}
}
