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
	 * この物理量インスタンスをkg,m,s,Aで表したときの数値の部分。
	 * nullの場合はこのインスタンスが単位を示すものであることを意味する。
	 */
	public final Double Number;
	public final int mDegree,kgDegree,sDegree,ADegree;

	/*
	 * コンストラクタ。
	 * 指定された文字列により物理量(または数値、単位のみ)を表すインスタンスを生成する。
	 * */
	public PhysicalQuantity(String valueStr) throws IllegalArgumentException,NullPointerException{
		if(valueStr == null) {
			throw new NullPointerException();
		}

		HashMap<String,Number> result = UnitEditor.dimensionAnalysis(valueStr);

		Double Number = (result.get("Number") == null)? null : (Double)result.get("Number") * Math.pow(10, (Integer) result.getOrDefault("none", 0));
		Integer mDegree = (Integer)result.getOrDefault("m", 0),
				kgDegree = (Integer)result.getOrDefault("kg",0),
				sDegree = (Integer)result.getOrDefault("s", 0),
				ADegree = (Integer)result.getOrDefault("A", 0);

		//数値も次元もないインスタンスは生成させない
		if(Number == null && kgDegree == 0 && mDegree == 0 && sDegree == 0 && ADegree == 0) {
			throw new IllegalArgumentException();
		}

		this.Number = Number;
		this.mDegree = mDegree;
		this.kgDegree = kgDegree;
		this.sDegree = sDegree;
		this.ADegree = ADegree;
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
		if(!this.equalsDimension(other) || this.Number == null || other.Number == null) {
			//次元が違うものや、ただの単位同士では比較できない
			throw new IllegalArgumentException("比較対象が不正です");
		}

		if(this.Number > other.Number){
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

			joiner.add(Double.toString(((Number == null)? 1:Number)*Math.pow(10, -kgDegree*(g_pre.n-3) -mDegree*m_pre.n -sDegree*s_pre.n -ADegree*A_pre.n)));

			Prefixs[] prefix = new Prefixs[]{g_pre, m_pre, s_pre, A_pre};
			int[] degree = new int[] {kgDegree, mDegree, sDegree, ADegree};
			for(int i=0;i<4;i++) {
				if(degree[i] == 0) {
					continue;
				}
				String sub =
						(prefix[i].equals(Prefixs.none)? "":prefix[i].toString())
						+((i==0)? "g":(i==1)? "m":(i==2)? "s": "A")
						+((degree[i]==1)? "":degree[i]);
				joiner.add(sub);
			}

			return joiner.toString();
		}else {
			return Double.toString(Number);
		}
	}


	/*
	 * この物理量を構成する要素群の内、kg,m,s,Aの数値、即ち次元が、
	 * 指定の他の要素群と一致するかを判定する。
	 * @param 比較対象のインスタンス
	 * @return 次元が一致する場合true
	 * */
	public boolean equalsDimension(PhysicalQuantity other) {
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
	 * このインスタンスが無次元量なのかを判定する
	 * @return 無次元量である場合true
	 * */
	public boolean isNonDimension() {
		return kgDegree == 0 && mDegree == 0 && sDegree == 0 && ADegree == 0;
	}

	/*
	 * このインスタンスをMKSA単位系で表示したときの数値部分を返す。
	 * @return MKSA単位系における数値を表すDoubleインスタンス
	 * */
	public Double getNumber() {
		return this.Number;
	}
}
