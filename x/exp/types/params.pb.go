// Code generated by protoc-gen-gogo. DO NOT EDIT.
// source: craft/exp/v1beta1/params.proto

package types

import (
	fmt "fmt"
	_ "github.com/cosmos/cosmos-sdk/types"
	_ "github.com/cosmos/cosmos-sdk/x/auth/types"
	_ "github.com/gogo/protobuf/gogoproto"
	proto "github.com/gogo/protobuf/proto"
	github_com_gogo_protobuf_types "github.com/gogo/protobuf/types"
	_ "github.com/regen-network/cosmos-proto"
	_ "google.golang.org/protobuf/types/known/durationpb"
	io "io"
	math "math"
	math_bits "math/bits"
	time "time"
)

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf
var _ = time.Kitchen

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.GoGoProtoPackageIsVersion3 // please upgrade the proto package

// Params holds parameters for the exp module
type Params struct {
	MaxCoinMint      uint64        `protobuf:"varint,1,opt,name=max_coin_mint,json=maxCoinMint,proto3,castrepeated=github.com/cosmos/cosmos-sdk/types.Coins" json:"max_coin_mint,omitempty" yaml:"max_coin_mint"`
	DaoAccount       string        `protobuf:"bytes,2,opt,name=daoAccount,proto3" json:"daoAccount,omitempty"`
	Denom            string        `protobuf:"bytes,3,opt,name=denom,proto3" json:"denom,omitempty"`
	IbcAssetDenom    string        `protobuf:"bytes,4,opt,name=ibc_asset_denom,json=ibcAssetDenom,proto3" json:"ibc_asset_denom,omitempty"`
	ClosePoolPeriod  time.Duration `protobuf:"bytes,5,opt,name=close_pool_period,json=closePoolPeriod,proto3,stdduration" json:"close_pool_period"`
	VestingPeriodEnd time.Duration `protobuf:"bytes,6,opt,name=vesting_period_end,json=vestingPeriodEnd,proto3,stdduration" json:"vesting_period_end"`
	BurnExpPeriod    time.Duration `protobuf:"bytes,7,opt,name=burn_exp_period,json=burnExpPeriod,proto3,stdduration" json:"burn_exp_period"`
}

func (m *Params) Reset()         { *m = Params{} }
func (m *Params) String() string { return proto.CompactTextString(m) }
func (*Params) ProtoMessage()    {}
func (*Params) Descriptor() ([]byte, []int) {
	return fileDescriptor_5733908f4a86ced5, []int{0}
}
func (m *Params) XXX_Unmarshal(b []byte) error {
	return m.Unmarshal(b)
}
func (m *Params) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	if deterministic {
		return xxx_messageInfo_Params.Marshal(b, m, deterministic)
	} else {
		b = b[:cap(b)]
		n, err := m.MarshalToSizedBuffer(b)
		if err != nil {
			return nil, err
		}
		return b[:n], nil
	}
}
func (m *Params) XXX_Merge(src proto.Message) {
	xxx_messageInfo_Params.Merge(m, src)
}
func (m *Params) XXX_Size() int {
	return m.Size()
}
func (m *Params) XXX_DiscardUnknown() {
	xxx_messageInfo_Params.DiscardUnknown(m)
}

var xxx_messageInfo_Params proto.InternalMessageInfo

func (m *Params) GetMaxCoinMint() uint64 {
	if m != nil {
		return m.MaxCoinMint
	}
	return 0
}

func (m *Params) GetDaoAccount() string {
	if m != nil {
		return m.DaoAccount
	}
	return ""
}

func (m *Params) GetDenom() string {
	if m != nil {
		return m.Denom
	}
	return ""
}

func (m *Params) GetIbcAssetDenom() string {
	if m != nil {
		return m.IbcAssetDenom
	}
	return ""
}

func (m *Params) GetClosePoolPeriod() time.Duration {
	if m != nil {
		return m.ClosePoolPeriod
	}
	return 0
}

func (m *Params) GetVestingPeriodEnd() time.Duration {
	if m != nil {
		return m.VestingPeriodEnd
	}
	return 0
}

func (m *Params) GetBurnExpPeriod() time.Duration {
	if m != nil {
		return m.BurnExpPeriod
	}
	return 0
}

func init() {
	proto.RegisterType((*Params)(nil), "craft.exp.v1beta1.Params")
}

func init() { proto.RegisterFile("craft/exp/v1beta1/params.proto", fileDescriptor_5733908f4a86ced5) }

var fileDescriptor_5733908f4a86ced5 = []byte{
	// 447 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0x8c, 0x92, 0xbf, 0x6e, 0xdb, 0x3c,
	0x14, 0xc5, 0xad, 0x2f, 0x89, 0xbf, 0x96, 0x86, 0xe1, 0x46, 0xf0, 0xa0, 0x64, 0xa0, 0x0c, 0x03,
	0x2d, 0xbc, 0x54, 0x42, 0xda, 0xad, 0x5b, 0xdc, 0x04, 0x28, 0x50, 0x14, 0x75, 0x3d, 0x76, 0x21,
	0x28, 0x8a, 0x56, 0x88, 0x4a, 0xbc, 0x84, 0x48, 0x05, 0xca, 0x5b, 0x74, 0xec, 0x33, 0xf4, 0x3d,
	0x0a, 0x78, 0xcc, 0xd8, 0xc9, 0x29, 0xec, 0x37, 0xe8, 0x13, 0x14, 0x22, 0xe9, 0xa0, 0x7f, 0x96,
	0x4c, 0xe2, 0xe1, 0xef, 0xde, 0x73, 0xc8, 0x2b, 0x22, 0xcc, 0x6a, 0xba, 0x32, 0x29, 0x6f, 0x55,
	0x7a, 0x7d, 0x96, 0x71, 0x43, 0xcf, 0x52, 0x45, 0x6b, 0x5a, 0xe9, 0x44, 0xd5, 0x60, 0x20, 0x3c,
	0xb6, 0x3c, 0xe1, 0xad, 0x4a, 0x3c, 0x3f, 0x1d, 0x17, 0x50, 0x80, 0xa5, 0x69, 0xb7, 0x72, 0x85,
	0xa7, 0x27, 0x0c, 0x74, 0x05, 0x9a, 0x38, 0xe0, 0x84, 0x47, 0xd8, 0xa9, 0x34, 0xa3, 0x9a, 0xdf,
	0xa7, 0x30, 0x10, 0xf2, 0x2f, 0x4e, 0x1b, 0x73, 0x75, 0xcf, 0x3b, 0xe1, 0x79, 0xfc, 0xef, 0x19,
	0x73, 0x0a, 0x42, 0xae, 0xf6, 0xd9, 0xb8, 0x00, 0x28, 0x4a, 0x9e, 0x5a, 0x95, 0x35, 0xab, 0x34,
	0x6f, 0x6a, 0x6a, 0x04, 0xf8, 0x80, 0xe9, 0xb7, 0x03, 0xd4, 0x5f, 0xd8, 0x5b, 0x85, 0x25, 0x1a,
	0x56, 0xb4, 0x25, 0x5d, 0x3a, 0xa9, 0x84, 0x34, 0x51, 0x30, 0x09, 0x66, 0x87, 0xf3, 0x37, 0xeb,
	0x4d, 0x1c, 0xfc, 0xdc, 0xc4, 0xe3, 0x1b, 0x5a, 0x95, 0xaf, 0xa6, 0x7f, 0x94, 0x4c, 0xbf, 0xde,
	0xc5, 0xb3, 0x42, 0x98, 0xab, 0x26, 0x4b, 0x18, 0x54, 0xfe, 0x6e, 0xfe, 0xf3, 0x5c, 0xe7, 0x9f,
	0x52, 0x73, 0xa3, 0xb8, 0x4e, 0x5e, 0x83, 0x90, 0x7a, 0x39, 0xa8, 0x68, 0xdb, 0xad, 0xde, 0x09,
	0x69, 0x42, 0x8c, 0x50, 0x4e, 0xe1, 0x9c, 0x31, 0x68, 0xa4, 0x89, 0xfe, 0x9b, 0x04, 0xb3, 0xc7,
	0xcb, 0xdf, 0x76, 0xc2, 0x31, 0x3a, 0xca, 0xb9, 0x84, 0x2a, 0x3a, 0xb0, 0xc8, 0x89, 0xf0, 0x19,
	0x1a, 0x89, 0x8c, 0x11, 0xaa, 0x35, 0x37, 0xc4, 0xf1, 0x43, 0xcb, 0x87, 0x22, 0x63, 0xe7, 0xdd,
	0xee, 0x85, 0xad, 0x7b, 0x8f, 0x8e, 0x59, 0x09, 0x9a, 0x13, 0x05, 0x50, 0x12, 0xc5, 0x6b, 0x01,
	0x79, 0x74, 0x34, 0x09, 0x66, 0x83, 0x17, 0x27, 0x89, 0x1b, 0x49, 0xb2, 0x1f, 0x49, 0x72, 0xe1,
	0x47, 0x32, 0x7f, 0xb4, 0xde, 0xc4, 0xbd, 0x2f, 0x77, 0x71, 0xb0, 0x1c, 0xd9, 0xee, 0x05, 0x40,
	0xb9, 0xb0, 0xbd, 0xe1, 0x07, 0x14, 0x5e, 0x73, 0x6d, 0x84, 0x2c, 0xbc, 0x1b, 0xe1, 0x32, 0x8f,
	0xfa, 0x0f, 0x77, 0x7c, 0xe2, 0xdb, 0x9d, 0xdf, 0xa5, 0xcc, 0xc3, 0xb7, 0x68, 0x94, 0x35, 0xb5,
	0x24, 0xbc, 0x55, 0xfb, 0x13, 0xfe, 0xff, 0x70, 0xbf, 0x61, 0xd7, 0x7b, 0xd9, 0x2a, 0xe7, 0x37,
	0x7f, 0xba, 0xde, 0xe2, 0xe0, 0x76, 0x8b, 0x83, 0x1f, 0x5b, 0x1c, 0x7c, 0xde, 0xe1, 0xde, 0xed,
	0x0e, 0xf7, 0xbe, 0xef, 0x70, 0xef, 0xe3, 0xa0, 0xb5, 0xcf, 0xc3, 0xfe, 0x88, 0xac, 0x6f, 0x2d,
	0x5f, 0xfe, 0x0a, 0x00, 0x00, 0xff, 0xff, 0x03, 0xbf, 0xa9, 0xfc, 0xdc, 0x02, 0x00, 0x00,
}

func (m *Params) Marshal() (dAtA []byte, err error) {
	size := m.Size()
	dAtA = make([]byte, size)
	n, err := m.MarshalToSizedBuffer(dAtA[:size])
	if err != nil {
		return nil, err
	}
	return dAtA[:n], nil
}

func (m *Params) MarshalTo(dAtA []byte) (int, error) {
	size := m.Size()
	return m.MarshalToSizedBuffer(dAtA[:size])
}

func (m *Params) MarshalToSizedBuffer(dAtA []byte) (int, error) {
	i := len(dAtA)
	_ = i
	var l int
	_ = l
	n1, err1 := github_com_gogo_protobuf_types.StdDurationMarshalTo(m.BurnExpPeriod, dAtA[i-github_com_gogo_protobuf_types.SizeOfStdDuration(m.BurnExpPeriod):])
	if err1 != nil {
		return 0, err1
	}
	i -= n1
	i = encodeVarintParams(dAtA, i, uint64(n1))
	i--
	dAtA[i] = 0x3a
	n2, err2 := github_com_gogo_protobuf_types.StdDurationMarshalTo(m.VestingPeriodEnd, dAtA[i-github_com_gogo_protobuf_types.SizeOfStdDuration(m.VestingPeriodEnd):])
	if err2 != nil {
		return 0, err2
	}
	i -= n2
	i = encodeVarintParams(dAtA, i, uint64(n2))
	i--
	dAtA[i] = 0x32
	n3, err3 := github_com_gogo_protobuf_types.StdDurationMarshalTo(m.ClosePoolPeriod, dAtA[i-github_com_gogo_protobuf_types.SizeOfStdDuration(m.ClosePoolPeriod):])
	if err3 != nil {
		return 0, err3
	}
	i -= n3
	i = encodeVarintParams(dAtA, i, uint64(n3))
	i--
	dAtA[i] = 0x2a
	if len(m.IbcAssetDenom) > 0 {
		i -= len(m.IbcAssetDenom)
		copy(dAtA[i:], m.IbcAssetDenom)
		i = encodeVarintParams(dAtA, i, uint64(len(m.IbcAssetDenom)))
		i--
		dAtA[i] = 0x22
	}
	if len(m.Denom) > 0 {
		i -= len(m.Denom)
		copy(dAtA[i:], m.Denom)
		i = encodeVarintParams(dAtA, i, uint64(len(m.Denom)))
		i--
		dAtA[i] = 0x1a
	}
	if len(m.DaoAccount) > 0 {
		i -= len(m.DaoAccount)
		copy(dAtA[i:], m.DaoAccount)
		i = encodeVarintParams(dAtA, i, uint64(len(m.DaoAccount)))
		i--
		dAtA[i] = 0x12
	}
	if m.MaxCoinMint != 0 {
		i = encodeVarintParams(dAtA, i, uint64(m.MaxCoinMint))
		i--
		dAtA[i] = 0x8
	}
	return len(dAtA) - i, nil
}

func encodeVarintParams(dAtA []byte, offset int, v uint64) int {
	offset -= sovParams(v)
	base := offset
	for v >= 1<<7 {
		dAtA[offset] = uint8(v&0x7f | 0x80)
		v >>= 7
		offset++
	}
	dAtA[offset] = uint8(v)
	return base
}
func (m *Params) Size() (n int) {
	if m == nil {
		return 0
	}
	var l int
	_ = l
	if m.MaxCoinMint != 0 {
		n += 1 + sovParams(uint64(m.MaxCoinMint))
	}
	l = len(m.DaoAccount)
	if l > 0 {
		n += 1 + l + sovParams(uint64(l))
	}
	l = len(m.Denom)
	if l > 0 {
		n += 1 + l + sovParams(uint64(l))
	}
	l = len(m.IbcAssetDenom)
	if l > 0 {
		n += 1 + l + sovParams(uint64(l))
	}
	l = github_com_gogo_protobuf_types.SizeOfStdDuration(m.ClosePoolPeriod)
	n += 1 + l + sovParams(uint64(l))
	l = github_com_gogo_protobuf_types.SizeOfStdDuration(m.VestingPeriodEnd)
	n += 1 + l + sovParams(uint64(l))
	l = github_com_gogo_protobuf_types.SizeOfStdDuration(m.BurnExpPeriod)
	n += 1 + l + sovParams(uint64(l))
	return n
}

func sovParams(x uint64) (n int) {
	return (math_bits.Len64(x|1) + 6) / 7
}
func sozParams(x uint64) (n int) {
	return sovParams(uint64((x << 1) ^ uint64((int64(x) >> 63))))
}
func (m *Params) Unmarshal(dAtA []byte) error {
	l := len(dAtA)
	iNdEx := 0
	for iNdEx < l {
		preIndex := iNdEx
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return ErrIntOverflowParams
			}
			if iNdEx >= l {
				return io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= uint64(b&0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		fieldNum := int32(wire >> 3)
		wireType := int(wire & 0x7)
		if wireType == 4 {
			return fmt.Errorf("proto: Params: wiretype end group for non-group")
		}
		if fieldNum <= 0 {
			return fmt.Errorf("proto: Params: illegal tag %d (wire type %d)", fieldNum, wire)
		}
		switch fieldNum {
		case 1:
			if wireType != 0 {
				return fmt.Errorf("proto: wrong wireType = %d for field MaxCoinMint", wireType)
			}
			m.MaxCoinMint = 0
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowParams
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				m.MaxCoinMint |= uint64(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
		case 2:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field DaoAccount", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowParams
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= uint64(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthParams
			}
			postIndex := iNdEx + intStringLen
			if postIndex < 0 {
				return ErrInvalidLengthParams
			}
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.DaoAccount = string(dAtA[iNdEx:postIndex])
			iNdEx = postIndex
		case 3:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field Denom", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowParams
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= uint64(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthParams
			}
			postIndex := iNdEx + intStringLen
			if postIndex < 0 {
				return ErrInvalidLengthParams
			}
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.Denom = string(dAtA[iNdEx:postIndex])
			iNdEx = postIndex
		case 4:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field IbcAssetDenom", wireType)
			}
			var stringLen uint64
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowParams
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				stringLen |= uint64(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			intStringLen := int(stringLen)
			if intStringLen < 0 {
				return ErrInvalidLengthParams
			}
			postIndex := iNdEx + intStringLen
			if postIndex < 0 {
				return ErrInvalidLengthParams
			}
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			m.IbcAssetDenom = string(dAtA[iNdEx:postIndex])
			iNdEx = postIndex
		case 5:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field ClosePoolPeriod", wireType)
			}
			var msglen int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowParams
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				msglen |= int(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if msglen < 0 {
				return ErrInvalidLengthParams
			}
			postIndex := iNdEx + msglen
			if postIndex < 0 {
				return ErrInvalidLengthParams
			}
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			if err := github_com_gogo_protobuf_types.StdDurationUnmarshal(&m.ClosePoolPeriod, dAtA[iNdEx:postIndex]); err != nil {
				return err
			}
			iNdEx = postIndex
		case 6:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field VestingPeriodEnd", wireType)
			}
			var msglen int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowParams
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				msglen |= int(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if msglen < 0 {
				return ErrInvalidLengthParams
			}
			postIndex := iNdEx + msglen
			if postIndex < 0 {
				return ErrInvalidLengthParams
			}
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			if err := github_com_gogo_protobuf_types.StdDurationUnmarshal(&m.VestingPeriodEnd, dAtA[iNdEx:postIndex]); err != nil {
				return err
			}
			iNdEx = postIndex
		case 7:
			if wireType != 2 {
				return fmt.Errorf("proto: wrong wireType = %d for field BurnExpPeriod", wireType)
			}
			var msglen int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return ErrIntOverflowParams
				}
				if iNdEx >= l {
					return io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				msglen |= int(b&0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if msglen < 0 {
				return ErrInvalidLengthParams
			}
			postIndex := iNdEx + msglen
			if postIndex < 0 {
				return ErrInvalidLengthParams
			}
			if postIndex > l {
				return io.ErrUnexpectedEOF
			}
			if err := github_com_gogo_protobuf_types.StdDurationUnmarshal(&m.BurnExpPeriod, dAtA[iNdEx:postIndex]); err != nil {
				return err
			}
			iNdEx = postIndex
		default:
			iNdEx = preIndex
			skippy, err := skipParams(dAtA[iNdEx:])
			if err != nil {
				return err
			}
			if (skippy < 0) || (iNdEx+skippy) < 0 {
				return ErrInvalidLengthParams
			}
			if (iNdEx + skippy) > l {
				return io.ErrUnexpectedEOF
			}
			iNdEx += skippy
		}
	}

	if iNdEx > l {
		return io.ErrUnexpectedEOF
	}
	return nil
}
func skipParams(dAtA []byte) (n int, err error) {
	l := len(dAtA)
	iNdEx := 0
	depth := 0
	for iNdEx < l {
		var wire uint64
		for shift := uint(0); ; shift += 7 {
			if shift >= 64 {
				return 0, ErrIntOverflowParams
			}
			if iNdEx >= l {
				return 0, io.ErrUnexpectedEOF
			}
			b := dAtA[iNdEx]
			iNdEx++
			wire |= (uint64(b) & 0x7F) << shift
			if b < 0x80 {
				break
			}
		}
		wireType := int(wire & 0x7)
		switch wireType {
		case 0:
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return 0, ErrIntOverflowParams
				}
				if iNdEx >= l {
					return 0, io.ErrUnexpectedEOF
				}
				iNdEx++
				if dAtA[iNdEx-1] < 0x80 {
					break
				}
			}
		case 1:
			iNdEx += 8
		case 2:
			var length int
			for shift := uint(0); ; shift += 7 {
				if shift >= 64 {
					return 0, ErrIntOverflowParams
				}
				if iNdEx >= l {
					return 0, io.ErrUnexpectedEOF
				}
				b := dAtA[iNdEx]
				iNdEx++
				length |= (int(b) & 0x7F) << shift
				if b < 0x80 {
					break
				}
			}
			if length < 0 {
				return 0, ErrInvalidLengthParams
			}
			iNdEx += length
		case 3:
			depth++
		case 4:
			if depth == 0 {
				return 0, ErrUnexpectedEndOfGroupParams
			}
			depth--
		case 5:
			iNdEx += 4
		default:
			return 0, fmt.Errorf("proto: illegal wireType %d", wireType)
		}
		if iNdEx < 0 {
			return 0, ErrInvalidLengthParams
		}
		if depth == 0 {
			return iNdEx, nil
		}
	}
	return 0, io.ErrUnexpectedEOF
}

var (
	ErrInvalidLengthParams        = fmt.Errorf("proto: negative length found during unmarshaling")
	ErrIntOverflowParams          = fmt.Errorf("proto: integer overflow")
	ErrUnexpectedEndOfGroupParams = fmt.Errorf("proto: unexpected end of group")
)
